CREATE TABLE authors (
    id VARCHAR(50) PRIMARY KEY,
    full_name VARCHAR NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE works (
    id VARCHAR(50) PRIMARY KEY,
    title VARCHAR NOT NULL,
    description VARCHAR,
    subjects VARCHAR,
    created_at TIMESTAMP NOT NULL
);

CREATE TABLE work_authors (
    work_id VARCHAR NOT NULL,
    author_id VARCHAR NOT NULL,
    FOREIGN KEY (work_id) REFERENCES works(id),
    FOREIGN KEY (author_id) REFERENCES authors(id),
    PRIMARY KEY (work_id, author_id)
);

CREATE TABLE users (
    id UUID PRIMARY KEY,
    email VARCHAR NOT NULL,
    full_name VARCHAR NOT NULL,
    password VARCHAR NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE reviews (
    id UUID PRIMARY KEY,
    work_id VARCHAR NOT NULL,
    user_id UUID NOT NULL,
    created_at TIMESTAMP NOT NULL,
    rating INT NOT NULL,
    FOREIGN KEY (work_id) REFERENCES works(id),
    FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE cf_recommendations (
    user_id UUID NOT NULL,
    work_id VARCHAR NOT NULL,
    score double precision NOT NULL,
    FOREIGN KEY (work_id) REFERENCES works(id),
    FOREIGN KEY (user_id) REFERENCES users(id),
    PRIMARY KEY (work_id, user_id)
);

CREATE INDEX cf_recommendations_score_idx ON cf_recommendations (score);

CREATE TABLE cb_recommendations (
    user_id UUID NOT NULL,
    work_id VARCHAR NOT NULL,
    score double precision NOT NULL,
    PRIMARY KEY (work_id, user_id),
    FOREIGN KEY (work_id) REFERENCES works(id),
    FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE INDEX cb_recommendations_score_idx ON cb_recommendations (score);

CREATE TABLE cbr_requests (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    created_at TIMESTAMP NOT NULL,
    completed_at TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE INDEX cbr_requests_user_id_idx ON cbr_requests (user_id);
CREATE INDEX cbr_requests_completed_at_idx ON cbr_requests (completed_at);