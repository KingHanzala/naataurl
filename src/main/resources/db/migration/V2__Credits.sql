ALTER TABLE public.users DROP COLUMN IF EXISTS usage_credits;
ALTER TABLE public.users ADD COLUMN usage_credits BIGINT DEFAULT 5;

ALTER TABLE public.url_mapper DROP COLUMN IF EXISTS url_clicks;
ALTER TABLE public.url_mapper ADD COLUMN url_clicks BIGINT DEFAULT 0;