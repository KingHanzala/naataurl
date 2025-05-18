DROP Sequence if exists o1_url_id_seq;
CREATE Sequence o1_url_id_seq;

DROP TABLE IF EXISTS public.url_mapper;
CREATE TABLE public.url_mapper (
    url_id BIGINT NOT NULL default nextval('o1_url_id_seq'),
    original_url VARCHAR(2048) NOT NULL,
    short_url VARCHAR(128) NOT NULL UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP,
    is_deleted BOOLEAN DEFAULT FALSE,
    user_id BIGSERIAL NOT NULL,
    CONSTRAINT url_mapper_pkey PRIMARY KEY (url_id),
    CONSTRAINT fk_user
        FOREIGN KEY(user_id) 
        REFERENCES public.users(user_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

CREATE TRIGGER set_updated_at
BEFORE UPDATE ON public.url_mapper
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();
