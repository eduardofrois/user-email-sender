# User Service CRUD Design

## Context

The `user` module currently exposes user creation, batch creation, list, and lookup operations. The controller maps request DTOs to JPA entities directly, responses expose `UserModel`, and some reads trigger RabbitMQ events. The existing `updateUser` method does not apply the incoming data.

The goal is to make the user service more robust first, with clear REST endpoints, DTOs, mappers, service method names, and error handling.

## Scope

Implement a complete CRUD surface for users:

- `POST /api/v1/users`
- `POST /api/v1/users/batch`
- `GET /api/v1/users`
- `GET /api/v1/users/{userId}`
- `PUT /api/v1/users/{userId}`
- `PATCH /api/v1/users/{userId}`
- `DELETE /api/v1/users/{userId}`

Keep the current RabbitMQ integration for operations that create batch/email work. Reads must not publish events.

## API Design

`POST /api/v1/users` creates one user from a validated request body and returns `201 Created` with a user response DTO.

`POST /api/v1/users/batch` creates multiple users from a validated list and returns `201 Created` with a list of user response DTOs.

`GET /api/v1/users` returns all users with `200 OK`.

`GET /api/v1/users/{userId}` returns a single user with `200 OK`, or `404 Not Found` when the UUID does not exist.

`PUT /api/v1/users/{userId}` replaces editable user fields. Both `name` and `email` are required.

`PATCH /api/v1/users/{userId}` updates only provided fields. Blank values are invalid when provided.

`DELETE /api/v1/users/{userId}` deletes an existing user and returns `204 No Content`.

## DTOs

Use request and response DTOs instead of exposing `UserModel` from controllers:

- `CreateUserRequest`: `name`, `email`
- `UpdateUserRequest`: `name`, `email`
- `PatchUserRequest`: optional `name`, optional `email`
- `UserResponse`: `userId`, `name`, `email`

Use Jakarta validation annotations on request DTOs. `PatchUserRequest` needs custom validation semantics through service checks or nullable constraints so omitted fields remain valid.

## Mapping

Make `UserMapper` a Spring component responsible for:

- `toModel(CreateUserRequest request)`
- `toResponse(UserModel user)`
- `toResponseList(List<UserModel> users)`
- `updateModel(UserModel user, UpdateUserRequest request)`
- `patchModel(UserModel user, PatchUserRequest request)`

Controller code should not use `BeanUtils` or instantiate `UserModel`.

## Service Design

Rename service methods to describe business operations:

- `createUser`
- `createUsers`
- `findAllUsers`
- `findUserById`
- `updateUser`
- `patchUser`
- `deleteUser`

Use a private `getUserOrThrow(UUID userId)` helper to centralize not-found behavior.

Creation methods should save entities and then publish the existing RabbitMQ events. Reads should only read from the repository. Updates and deletes do not need new events in this iteration because the current consumer/event model only defines creation, list request, and simulated delay semantics.

## Error Handling

Add a domain exception such as `UserNotFoundException` and a `@RestControllerAdvice` to return consistent HTTP error responses.

Expected errors:

- `404 Not Found` for missing users.
- `400 Bad Request` for validation failures.

Email uniqueness is not included in this iteration because the current database schema has no unique constraint. Adding it later should include both repository checks and a Flyway migration.

## Data Model

Keep the existing table `tb_users` and columns `user_id`, `name`, and `email`.

Normalize the Java field name from `UserId` to `userId` while preserving the database column mapping through `@Column(name = "user_id")`.

## Testing And Verification

Run the user module tests with Maven after implementation.

Minimum verification:

- Application context loads.
- Controller/service compile with new DTOs and mapper.
- CRUD methods compile and handle not-found paths.
- Existing producer mapper still receives user IDs, names, and emails correctly.

