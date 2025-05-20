ALTER TABLE public.users DROP COLUMN IF EXISTS confirmation_token;
ALTER TABLE public.users ADD COLUMN confirmation_token VARCHAR(128);

ALTER TABLE public.users DROP COLUMN IF EXISTS token_expiry;
ALTER TABLE public.users ADD COLUMN token_expiry TIMESTAMP WITHOUT TIME ZONE;

ALTER TABLE public.users DROP COLUMN IF EXISTS oauth2_login;
ALTER TABLE public.users ADD COLUMN oauth2_login BOOLEAN DEFAULT FALSE;

ALTER TABLE public.users DROP COLUMN IF EXISTS verified;
ALTER TABLE public.users ADD COLUMN verified BOOLEAN DEFAULT FALSE;
