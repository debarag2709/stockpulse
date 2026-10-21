
ALTER TABLE users
    ADD COLUMN whatsapp_phone VARCHAR(20),
    ADD COLUMN whatsapp_enabled BOOLEAN NOT NULL DEFAULT FALSE;
