ALTER TABLE product ADD COLUMN image_key character varying(500);

ALTER TABLE product ADD COLUMN category character varying(50);

ALTER TABLE product ADD CONSTRAINT product_category_check
    CHECK (((category)::text = ANY ((ARRAY[
    'ELECTRONICS'::character varying,
    'BEAUTY'::character varying,
    'FASHION'::character varying,
    'LIVING'::character varying,
    'FOOD'::character varying,
    'TOYS'::character varying,
    'OUTDOOR'::character varying,
    'PET'::character varying,
    'KITCHEN'::character varying,
    'STATIONERY'::character varying
    ])::text[])));
