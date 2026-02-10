-- =============================================================================
-- V1.2.0: Seed - Members, Member Replicas, Funding Members
-- =============================================================================

INSERT INTO members (id, email, nickname, birthday, role, address, phone_num, name, status, auth_sub,
                     created_at, updated_at, created_by, updated_by)
VALUES
    (1, 'qa-seller-giftify@team201.dev', '멍청한돼지0009', '1975-11-08', 'SELLER',
     '서울시 종로구', '010-1234-5678', '김주영', 'ACTIVE', 'auth0|6981838d48f8397cae06ddb0',
     NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
    (2, 'qa-buyer-giftify@team201.dev', '나른한고양이0013', '2003-02-14', 'BUYER',
     '서울시 송파구', '010-5678-1234', '김영주', 'ACTIVE', 'auth0|698183a503a368a7b14ca6ab',
     NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
    (3, 'qa-seller-giftify@naver.com', '멍청한고양이2013', '2003-02-14', 'SELLER',
     '서울시 송파구', '010-0002-9871', '김영주', 'ACTIVE', 'auth0|6981842c839dce07958f5a37',
     NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
    (4, 'admin-giftify@team201.dev', '관리자', '1999-01-01', 'ADMIN',
     '서울시 강남구', '010-0000-0000', 'TEAM201', 'ACTIVE', 'auth0|6981843a226ff0ca1e6a5ae8',
     NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
    (5, 'qa-buyer2-giftify@team201.dev', '졸린토끼0042', '1998-07-22', 'BUYER',
     '서울시 마포구', '010-3333-4444', '이수현', 'ACTIVE', 'auth0|698184500000000000000001',
     NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
    (6, 'qa-buyer3-giftify@team201.dev', '배고픈강아지0007', '2001-12-25', 'BUYER',
     '서울시 서초구', '010-5555-6666', '박지민', 'ACTIVE', 'auth0|698184500000000000000002',
     NOW(), NOW(), 'SYSTEM', 'SYSTEM');

SELECT setval('members_id_seq', (SELECT MAX(id) FROM members));

INSERT INTO member_replica (id, nickname)
VALUES (1, '멍청한돼지0009'),
       (2, '나른한고양이0013'),
       (3, '멍청한고양이2013'),
       (4, '관리자'),
       (5, '졸린토끼0042'),
       (6, '배고픈강아지0007');

INSERT INTO funding_member (id, auth_sub, nickname)
VALUES (1, 'auth0|6981838d48f8397cae06ddb0', '멍청한돼지0009'),
       (2, 'auth0|698183a503a368a7b14ca6ab', '나른한고양이0013'),
       (3, 'auth0|6981842c839dce07958f5a37', '멍청한고양이2013'),
       (4, 'auth0|6981843a226ff0ca1e6a5ae8', '관리자'),
       (5, 'auth0|698184500000000000000001', '졸린토끼0042'),
       (6, 'auth0|698184500000000000000002', '배고픈강아지0007');
