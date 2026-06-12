CREATE TABLE tb_email (
    email_id UUID PRIMARY KEY,
    user_id UUID,
    email_from VARCHAR(255),
    email_to VARCHAR(255),
    email_subject VARCHAR(255),
    body TEXT,
    send_date_email TIMESTAMP,
    status_email VARCHAR(50)
);

INSERT INTO tb_email (
    email_id, user_id, email_from, email_to, email_subject, body, send_date_email, status_email
) VALUES (
    '11111111-1111-1111-1111-111111111111',
    '22222222-2222-2222-2222-222222222222',
    'exemplo@dominio.com',
    'destino@dominio.com',
    'Assunto de Exemplo',
    'Corpo do email de exemplo.',
    NOW(),
    'SENT'
);
