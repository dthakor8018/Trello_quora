--These records are stored in the database to test the Quora Application

--Insert values in USERS table
INSERT INTO quora.users(id, uuid, first_name, last_name, user_name, email, password, salt, country, about_me, dob, role, contact_number)
    	VALUES (1025,'database_uuid','database_first_name','database_last_name','database_user_name','database_email','database_password','database_salt', 'database_country' ,'database_about_me' ,'database_dob' , 'admin' , 'database_contact_number' );
INSERT INTO quora.users(id, uuid, first_name, last_name, user_name, email, password, salt, country, about_me, dob, role, contact_number)
     VALUES (1026,'database_uuid1','database_first_name1','database_last_name1','database_user_name1','database_email1','database_password1','database_salt1', 'database_country1' ,'database_about_me1' ,'database_dob1' , 'nonadmin' , 'database_contact_number1' );
INSERT INTO quora.users(id, uuid, first_name, last_name, user_name, email, password, salt, country, about_me, dob, role, contact_number)
    VALUES (1027,'database_uuid2','database_first_name2','database_last_name2','database_user_name2','database_email2','database_password2','database_salt2', 'database_country2' ,'database_about_me2' ,'database_dob2' , 'nonadmin' , 'database_contact_number2' );
INSERT INTO quora.users(id, uuid, first_name, last_name, user_name, email, password, salt, country, about_me, dob, role, contact_number)
    VALUES (1028,'database_uuid3','database_first_name3','database_last_name3','database_user_name3','database_email3','database_password3','database_salt3', 'database_country3' ,'database_about_me3' ,'database_dob3' , 'nonadmin' , 'database_contact_number3' );
INSERT INTO quora.users(id, uuid, first_name, last_name, user_name, email, password, salt, country, about_me, dob, role, contact_number)
    VALUES (1029,'database_uuid4','database_first_name4','database_last_name4','database_user_name4','database_email4','database_password4','database_salt4', 'database_country4' ,'database_about_me4' ,'database_dob4' , 'nonadmin' , 'database_contact_number4' );


--Insert values in USER_AUTH table
insert into quora.user_auth (id , uuid , user_id , access_token , expires_at , login_at, logout_at) values(1024 , 'database_uuid' , 1025 , 'database_accesstoken' , '2018-09-17 21:07:02.07' , '2018-09-17 13:07:02.07' , null);
insert into quora.user_auth (id , uuid , user_id , access_token , expires_at , login_at , logout_at) values(1025 , 'database_uuid1' , 1026 , 'database_accesstoken1' , '2018-09-17 21:07:02.07' , '2018-09-17 13:07:02.07' , null );
insert into quora.user_auth (id , uuid , user_id , access_token , expires_at , login_at , logout_at) values(1026 , 'database_uuid2' , 1027 , 'database_accesstoken2' , '2018-09-17 21:07:02.07' , '2018-09-17 13:07:02.07' , null );
insert into quora.user_auth (id , uuid , user_id , access_token , expires_at , login_at , logout_at) values(1027 , 'database_uuid3' , 1028 , 'database_accesstoken3' , '2018-09-17 21:07:02.07' , '2018-09-17 13:07:02.07' , '2018-09-17 15:07:02.07' );


--Insert values in QUESTION table
insert into quora.question (id,uuid,content,date,user_id) values(1024,'database_question_uuid','database_question_content','2018-09-17 19:41:19.593',1026);


--Insert values in ANSWER table
insert into quora.answer(id,uuid,ans,date,user_id,question_id) values (1024,'database_answer_uuid','my_answer','2018-09-17 19:41:19.593',1026,1024);
