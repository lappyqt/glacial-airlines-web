SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- Name: account; Type: SCHEMA; Schema: -; Owner: -
--

CREATE SCHEMA account;


--
-- Name: booking; Type: SCHEMA; Schema: -; Owner: -
--

CREATE SCHEMA booking;


--
-- Name: flight; Type: SCHEMA; Schema: -; Owner: -
--

CREATE SCHEMA flight;


SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: loyalty_account; Type: TABLE; Schema: account; Owner: -
--

CREATE TABLE account.loyalty_account (
    miles integer NOT NULL,
    created_at timestamp with time zone NOT NULL,
    id bigint NOT NULL
);


--
-- Name: loyalty_transaction; Type: TABLE; Schema: account; Owner: -
--

CREATE TABLE account.loyalty_transaction (
    miles integer NOT NULL,
    created_at timestamp with time zone NOT NULL,
    id bigint NOT NULL,
    loyalty_account_id bigint NOT NULL,
    order_id bigint,
    transaction_type character varying(20) NOT NULL,
    CONSTRAINT loyalty_transaction_transaction_type_check CHECK (((transaction_type)::text = ANY ((ARRAY['SPENT'::character varying, 'EARNED'::character varying, 'CANCELLED'::character varying, 'RETURNED'::character varying])::text[])))
);


--
-- Name: passenger; Type: TABLE; Schema: account; Owner: -
--

CREATE TABLE account.passenger (
    birth_date date,
    id bigint NOT NULL,
    contact_phone character varying(20),
    gender character varying(20),
    document_type character varying(30),
    document_number character varying(50),
    first_name character varying(100),
    last_name character varying(100),
    middle_name character varying(100),
    contact_email character varying(150),
    CONSTRAINT passenger_document_type_check CHECK (((document_type)::text = ANY ((ARRAY['PASSPORT_RUSSIAN'::character varying, 'INTERNATIONAL'::character varying, 'BIRTH_CERTIFICATE'::character varying])::text[]))),
    CONSTRAINT passenger_gender_check CHECK (((gender)::text = ANY ((ARRAY['MALE'::character varying, 'FEMALE'::character varying])::text[])))
);


--
-- Name: user_account; Type: TABLE; Schema: account; Owner: -
--

CREATE TABLE account.user_account (
    email_verified boolean NOT NULL,
    created_at timestamp with time zone NOT NULL,
    id bigint NOT NULL,
    loyalty_account_id bigint,
    passenger_id bigint,
    auth_provider character varying(20) NOT NULL,
    phone_number character varying(20) NOT NULL,
    password_hash character varying(68),
    first_name character varying(100) NOT NULL,
    last_name character varying(100) NOT NULL,
    middle_name character varying(100),
    email character varying(150) NOT NULL,
    CONSTRAINT user_account_auth_provider_check CHECK (((auth_provider)::text = ANY ((ARRAY['EMAIL'::character varying, 'GOOGLE'::character varying])::text[])))
);


--
-- Name: additional_service; Type: TABLE; Schema: booking; Owner: -
--

CREATE TABLE booking.additional_service (
    is_active boolean NOT NULL,
    price numeric(10,2) NOT NULL,
    id bigint NOT NULL,
    additional_service_type character varying(50) NOT NULL,
    name character varying(150) NOT NULL,
    description character varying(255) NOT NULL,
    CONSTRAINT additional_service_additional_service_type_check CHECK (((additional_service_type)::text = ANY ((ARRAY['MEAL'::character varying, 'REFUND'::character varying, 'EXTRA_BAGGAGE'::character varying])::text[])))
);


--
-- Name: booking_order; Type: TABLE; Schema: booking; Owner: -
--

CREATE TABLE booking.booking_order (
    base_price numeric(10,2),
    miles_earned integer NOT NULL,
    miles_spent integer NOT NULL,
    total_price numeric(10,2) NOT NULL,
    booking_expires_at timestamp with time zone NOT NULL,
    created_at timestamp with time zone NOT NULL,
    id bigint NOT NULL,
    outbound_flight_id bigint NOT NULL,
    return_flight_id bigint,
    returned_at timestamp with time zone,
    user_account_id bigint NOT NULL,
    contact_phone character varying(20),
    seat_class character varying(20) NOT NULL,
    status character varying(30) NOT NULL,
    payment_id character varying(100),
    contact_email character varying(150),
    CONSTRAINT booking_order_seat_class_check CHECK (((seat_class)::text = ANY ((ARRAY['ECONOMY'::character varying, 'EMERGENCY'::character varying, 'BUSINESS'::character varying])::text[]))),
    CONSTRAINT booking_order_status_check CHECK (((status)::text = ANY ((ARRAY['DRAFT'::character varying, 'PENDING_PAYMENT'::character varying, 'PAID'::character varying, 'CANCELLED'::character varying, 'COMPLETED'::character varying, 'RETURNED'::character varying])::text[])))
);


--
-- Name: order_passenger; Type: TABLE; Schema: booking; Owner: -
--

CREATE TABLE booking.order_passenger (
    birth_date date,
    id bigint NOT NULL,
    order_id bigint NOT NULL,
    outbound_seat_availability_id bigint,
    passenger_id bigint,
    passenger_type character varying(10) NOT NULL,
    gender character varying(20),
    document_type character varying(30),
    document_number character varying(50),
    first_name character varying(100),
    last_name character varying(100),
    middle_name character varying(100),
    CONSTRAINT order_passenger_document_type_check CHECK (((document_type)::text = ANY ((ARRAY['PASSPORT_RUSSIAN'::character varying, 'INTERNATIONAL'::character varying, 'BIRTH_CERTIFICATE'::character varying])::text[]))),
    CONSTRAINT order_passenger_gender_check CHECK (((gender)::text = ANY ((ARRAY['MALE'::character varying, 'FEMALE'::character varying])::text[]))),
    CONSTRAINT order_passenger_passenger_type_check CHECK (((passenger_type)::text = ANY ((ARRAY['ADULT'::character varying, 'CHILD'::character varying])::text[])))
);


--
-- Name: order_services; Type: TABLE; Schema: booking; Owner: -
--

CREATE TABLE booking.order_services (
    order_id bigint NOT NULL,
    service_id bigint NOT NULL
);


--
-- Name: aircraft; Type: TABLE; Schema: flight; Owner: -
--

CREATE TABLE flight.aircraft (
    business_seats integer NOT NULL,
    economy_seats integer NOT NULL,
    emergency_seats integer NOT NULL,
    id bigint NOT NULL,
    registration_number character varying(10) NOT NULL,
    model character varying(100) NOT NULL
);


--
-- Name: airport; Type: TABLE; Schema: flight; Owner: -
--

CREATE TABLE flight.airport (
    iata_code character varying(3) NOT NULL,
    offset_utc integer NOT NULL,
    id bigint NOT NULL,
    city character varying(150) NOT NULL,
    country character varying(150) NOT NULL,
    name character varying(150) NOT NULL
);


--
-- Name: flight; Type: TABLE; Schema: flight; Owner: -
--

CREATE TABLE flight.flight (
    aircraft_id bigint NOT NULL,
    arrival_time timestamp(6) without time zone NOT NULL,
    departure_time timestamp(6) without time zone NOT NULL,
    id bigint NOT NULL,
    route_id bigint NOT NULL,
    flight_number character varying(10) NOT NULL,
    status character varying(20) NOT NULL,
    CONSTRAINT flight_status_check CHECK (((status)::text = ANY ((ARRAY['SCHEDULED'::character varying, 'DELAYED'::character varying, 'CANCELLED'::character varying, 'COMPLETED'::character varying])::text[])))
);


--
-- Name: flight_inventory; Type: TABLE; Schema: flight; Owner: -
--

CREATE TABLE flight.flight_inventory (
    available_seats integer NOT NULL,
    child_seat_discount numeric(4,2) NOT NULL,
    price numeric(10,2) NOT NULL,
    flight_id bigint NOT NULL,
    id bigint NOT NULL,
    seat_class character varying(20) NOT NULL,
    CONSTRAINT flight_inventory_seat_class_check CHECK (((seat_class)::text = ANY ((ARRAY['ECONOMY'::character varying, 'EMERGENCY'::character varying, 'BUSINESS'::character varying])::text[])))
);


--
-- Name: route; Type: TABLE; Schema: flight; Owner: -
--

CREATE TABLE flight.route (
    arrival_airport_id bigint NOT NULL,
    departure_airport_id bigint NOT NULL,
    id bigint NOT NULL
);


--
-- Name: seat; Type: TABLE; Schema: flight; Owner: -
--

CREATE TABLE flight.seat (
    row_number integer NOT NULL,
    seat_letter character varying(1) NOT NULL,
    seat_number character varying(5) NOT NULL,
    aircraft_id bigint NOT NULL,
    id bigint NOT NULL,
    seat_class character varying(20) NOT NULL,
    CONSTRAINT seat_seat_class_check CHECK (((seat_class)::text = ANY ((ARRAY['ECONOMY'::character varying, 'EMERGENCY'::character varying, 'BUSINESS'::character varying])::text[])))
);


--
-- Name: seat_availability; Type: TABLE; Schema: flight; Owner: -
--

CREATE TABLE flight.seat_availability (
    flight_id bigint NOT NULL,
    id bigint NOT NULL,
    seat_id bigint NOT NULL,
    status character varying(20) NOT NULL,
    CONSTRAINT seat_availability_status_check CHECK (((status)::text = ANY ((ARRAY['AVAILABLE'::character varying, 'BOOKED'::character varying, 'OCCUPIED'::character varying])::text[])))
);


--
-- Name: account_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.account_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: additional_service_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.additional_service_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: aircraft_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.aircraft_id_seq
    START WITH 1
    INCREMENT BY 50
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: airport_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.airport_id_seq
    START WITH 1
    INCREMENT BY 50
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: booking_order_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.booking_order_id_seq
    START WITH 1
    INCREMENT BY 50
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: flight_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.flight_id_seq
    START WITH 1
    INCREMENT BY 50
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: flight_inventory_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.flight_inventory_id_seq
    START WITH 1
    INCREMENT BY 50
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: loyalty_account_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.loyalty_account_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: loyalty_transaction_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.loyalty_transaction_seq
    START WITH 1
    INCREMENT BY 50
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: order_passenger_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.order_passenger_id_seq
    START WITH 1
    INCREMENT BY 50
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: passenger_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.passenger_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: route_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.route_id_seq
    START WITH 1
    INCREMENT BY 50
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: seat_availability_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.seat_availability_id_seq
    START WITH 1
    INCREMENT BY 50
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: seat_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.seat_id_seq
    START WITH 1
    INCREMENT BY 50
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: loyalty_account loyalty_account_pkey; Type: CONSTRAINT; Schema: account; Owner: -
--

ALTER TABLE ONLY account.loyalty_account
    ADD CONSTRAINT loyalty_account_pkey PRIMARY KEY (id);


--
-- Name: loyalty_transaction loyalty_transaction_pkey; Type: CONSTRAINT; Schema: account; Owner: -
--

ALTER TABLE ONLY account.loyalty_transaction
    ADD CONSTRAINT loyalty_transaction_pkey PRIMARY KEY (id);


--
-- Name: passenger passenger_pkey; Type: CONSTRAINT; Schema: account; Owner: -
--

ALTER TABLE ONLY account.passenger
    ADD CONSTRAINT passenger_pkey PRIMARY KEY (id);


--
-- Name: user_account user_account_email_key; Type: CONSTRAINT; Schema: account; Owner: -
--

ALTER TABLE ONLY account.user_account
    ADD CONSTRAINT user_account_email_key UNIQUE (email);


--
-- Name: user_account user_account_loyalty_account_id_key; Type: CONSTRAINT; Schema: account; Owner: -
--

ALTER TABLE ONLY account.user_account
    ADD CONSTRAINT user_account_loyalty_account_id_key UNIQUE (loyalty_account_id);


--
-- Name: user_account user_account_passenger_id_key; Type: CONSTRAINT; Schema: account; Owner: -
--

ALTER TABLE ONLY account.user_account
    ADD CONSTRAINT user_account_passenger_id_key UNIQUE (passenger_id);


--
-- Name: user_account user_account_phone_number_key; Type: CONSTRAINT; Schema: account; Owner: -
--

ALTER TABLE ONLY account.user_account
    ADD CONSTRAINT user_account_phone_number_key UNIQUE (phone_number);


--
-- Name: user_account user_account_pkey; Type: CONSTRAINT; Schema: account; Owner: -
--

ALTER TABLE ONLY account.user_account
    ADD CONSTRAINT user_account_pkey PRIMARY KEY (id);


--
-- Name: additional_service additional_service_additional_service_type_key; Type: CONSTRAINT; Schema: booking; Owner: -
--

ALTER TABLE ONLY booking.additional_service
    ADD CONSTRAINT additional_service_additional_service_type_key UNIQUE (additional_service_type);


--
-- Name: additional_service additional_service_pkey; Type: CONSTRAINT; Schema: booking; Owner: -
--

ALTER TABLE ONLY booking.additional_service
    ADD CONSTRAINT additional_service_pkey PRIMARY KEY (id);


--
-- Name: booking_order booking_order_pkey; Type: CONSTRAINT; Schema: booking; Owner: -
--

ALTER TABLE ONLY booking.booking_order
    ADD CONSTRAINT booking_order_pkey PRIMARY KEY (id);


--
-- Name: order_passenger order_passenger_pkey; Type: CONSTRAINT; Schema: booking; Owner: -
--

ALTER TABLE ONLY booking.order_passenger
    ADD CONSTRAINT order_passenger_pkey PRIMARY KEY (id);


--
-- Name: aircraft aircraft_pkey; Type: CONSTRAINT; Schema: flight; Owner: -
--

ALTER TABLE ONLY flight.aircraft
    ADD CONSTRAINT aircraft_pkey PRIMARY KEY (id);


--
-- Name: aircraft aircraft_registration_number_key; Type: CONSTRAINT; Schema: flight; Owner: -
--

ALTER TABLE ONLY flight.aircraft
    ADD CONSTRAINT aircraft_registration_number_key UNIQUE (registration_number);


--
-- Name: airport airport_iata_code_key; Type: CONSTRAINT; Schema: flight; Owner: -
--

ALTER TABLE ONLY flight.airport
    ADD CONSTRAINT airport_iata_code_key UNIQUE (iata_code);


--
-- Name: airport airport_pkey; Type: CONSTRAINT; Schema: flight; Owner: -
--

ALTER TABLE ONLY flight.airport
    ADD CONSTRAINT airport_pkey PRIMARY KEY (id);


--
-- Name: flight flight_flight_number_departure_time_key; Type: CONSTRAINT; Schema: flight; Owner: -
--

ALTER TABLE ONLY flight.flight
    ADD CONSTRAINT flight_flight_number_departure_time_key UNIQUE (flight_number, departure_time);


--
-- Name: flight_inventory flight_inventory_flight_id_seat_class_key; Type: CONSTRAINT; Schema: flight; Owner: -
--

ALTER TABLE ONLY flight.flight_inventory
    ADD CONSTRAINT flight_inventory_flight_id_seat_class_key UNIQUE (flight_id, seat_class);


--
-- Name: flight_inventory flight_inventory_pkey; Type: CONSTRAINT; Schema: flight; Owner: -
--

ALTER TABLE ONLY flight.flight_inventory
    ADD CONSTRAINT flight_inventory_pkey PRIMARY KEY (id);


--
-- Name: flight flight_pkey; Type: CONSTRAINT; Schema: flight; Owner: -
--

ALTER TABLE ONLY flight.flight
    ADD CONSTRAINT flight_pkey PRIMARY KEY (id);


--
-- Name: route route_departure_airport_id_arrival_airport_id_key; Type: CONSTRAINT; Schema: flight; Owner: -
--

ALTER TABLE ONLY flight.route
    ADD CONSTRAINT route_departure_airport_id_arrival_airport_id_key UNIQUE (departure_airport_id, arrival_airport_id);


--
-- Name: route route_pkey; Type: CONSTRAINT; Schema: flight; Owner: -
--

ALTER TABLE ONLY flight.route
    ADD CONSTRAINT route_pkey PRIMARY KEY (id);


--
-- Name: seat seat_aircraft_id_seat_number_key; Type: CONSTRAINT; Schema: flight; Owner: -
--

ALTER TABLE ONLY flight.seat
    ADD CONSTRAINT seat_aircraft_id_seat_number_key UNIQUE (aircraft_id, seat_number);


--
-- Name: seat_availability seat_availability_flight_id_seat_id_key; Type: CONSTRAINT; Schema: flight; Owner: -
--

ALTER TABLE ONLY flight.seat_availability
    ADD CONSTRAINT seat_availability_flight_id_seat_id_key UNIQUE (flight_id, seat_id);


--
-- Name: seat_availability seat_availability_pkey; Type: CONSTRAINT; Schema: flight; Owner: -
--

ALTER TABLE ONLY flight.seat_availability
    ADD CONSTRAINT seat_availability_pkey PRIMARY KEY (id);


--
-- Name: seat seat_pkey; Type: CONSTRAINT; Schema: flight; Owner: -
--

ALTER TABLE ONLY flight.seat
    ADD CONSTRAINT seat_pkey PRIMARY KEY (id);


--
-- Name: loyalty_transaction fk392rkriaaguto2kr1mhy3u3ih; Type: FK CONSTRAINT; Schema: account; Owner: -
--

ALTER TABLE ONLY account.loyalty_transaction
    ADD CONSTRAINT fk392rkriaaguto2kr1mhy3u3ih FOREIGN KEY (loyalty_account_id) REFERENCES account.loyalty_account(id);


--
-- Name: user_account fkaq7gur4u1wdblws4y80f099vv; Type: FK CONSTRAINT; Schema: account; Owner: -
--

ALTER TABLE ONLY account.user_account
    ADD CONSTRAINT fkaq7gur4u1wdblws4y80f099vv FOREIGN KEY (loyalty_account_id) REFERENCES account.loyalty_account(id);


--
-- Name: user_account fkiifgnfux0rwb3j76jg6n4unec; Type: FK CONSTRAINT; Schema: account; Owner: -
--

ALTER TABLE ONLY account.user_account
    ADD CONSTRAINT fkiifgnfux0rwb3j76jg6n4unec FOREIGN KEY (passenger_id) REFERENCES account.passenger(id);


--
-- Name: loyalty_transaction fkpydhchbmh47oh50cxomxgwwih; Type: FK CONSTRAINT; Schema: account; Owner: -
--

ALTER TABLE ONLY account.loyalty_transaction
    ADD CONSTRAINT fkpydhchbmh47oh50cxomxgwwih FOREIGN KEY (order_id) REFERENCES booking.booking_order(id);


--
-- Name: booking_order fk6566de2gl9yisl172itmsvyxb; Type: FK CONSTRAINT; Schema: booking; Owner: -
--

ALTER TABLE ONLY booking.booking_order
    ADD CONSTRAINT fk6566de2gl9yisl172itmsvyxb FOREIGN KEY (user_account_id) REFERENCES account.user_account(id);


--
-- Name: order_services fkahqatgdqgadwgk7fwjqoi1f3c; Type: FK CONSTRAINT; Schema: booking; Owner: -
--

ALTER TABLE ONLY booking.order_services
    ADD CONSTRAINT fkahqatgdqgadwgk7fwjqoi1f3c FOREIGN KEY (order_id) REFERENCES booking.booking_order(id);


--
-- Name: booking_order fkf28wvonr2tpp5ws9hhlxw31gl; Type: FK CONSTRAINT; Schema: booking; Owner: -
--

ALTER TABLE ONLY booking.booking_order
    ADD CONSTRAINT fkf28wvonr2tpp5ws9hhlxw31gl FOREIGN KEY (return_flight_id) REFERENCES flight.flight(id);


--
-- Name: order_passenger fkf9kgbh3r7sm0wpcjmsp5xqc04; Type: FK CONSTRAINT; Schema: booking; Owner: -
--

ALTER TABLE ONLY booking.order_passenger
    ADD CONSTRAINT fkf9kgbh3r7sm0wpcjmsp5xqc04 FOREIGN KEY (passenger_id) REFERENCES account.passenger(id);


--
-- Name: booking_order fkmj34mj22xd0xcfusxjfdl9xgk; Type: FK CONSTRAINT; Schema: booking; Owner: -
--

ALTER TABLE ONLY booking.booking_order
    ADD CONSTRAINT fkmj34mj22xd0xcfusxjfdl9xgk FOREIGN KEY (outbound_flight_id) REFERENCES flight.flight(id);


--
-- Name: order_services fkn5hwlfpkashgphuj68nehdcby; Type: FK CONSTRAINT; Schema: booking; Owner: -
--

ALTER TABLE ONLY booking.order_services
    ADD CONSTRAINT fkn5hwlfpkashgphuj68nehdcby FOREIGN KEY (service_id) REFERENCES booking.additional_service(id);


--
-- Name: order_passenger fkpr36ecfn6dk423cr0octlg4b3; Type: FK CONSTRAINT; Schema: booking; Owner: -
--

ALTER TABLE ONLY booking.order_passenger
    ADD CONSTRAINT fkpr36ecfn6dk423cr0octlg4b3 FOREIGN KEY (outbound_seat_availability_id) REFERENCES flight.seat_availability(id);


--
-- Name: order_passenger fkqejdt3nry7i9giflm74e7vu73; Type: FK CONSTRAINT; Schema: booking; Owner: -
--

ALTER TABLE ONLY booking.order_passenger
    ADD CONSTRAINT fkqejdt3nry7i9giflm74e7vu73 FOREIGN KEY (order_id) REFERENCES booking.booking_order(id);


--
-- Name: flight fk13s4qgdsiygr6a578jbp0yjgb; Type: FK CONSTRAINT; Schema: flight; Owner: -
--

ALTER TABLE ONLY flight.flight
    ADD CONSTRAINT fk13s4qgdsiygr6a578jbp0yjgb FOREIGN KEY (route_id) REFERENCES flight.route(id);


--
-- Name: seat fk1xqnegav4xuo0hee2g5ftej31; Type: FK CONSTRAINT; Schema: flight; Owner: -
--

ALTER TABLE ONLY flight.seat
    ADD CONSTRAINT fk1xqnegav4xuo0hee2g5ftej31 FOREIGN KEY (aircraft_id) REFERENCES flight.aircraft(id);


--
-- Name: flight_inventory fk2dhy3sxfixxlnidmr6birg6gl; Type: FK CONSTRAINT; Schema: flight; Owner: -
--

ALTER TABLE ONLY flight.flight_inventory
    ADD CONSTRAINT fk2dhy3sxfixxlnidmr6birg6gl FOREIGN KEY (flight_id) REFERENCES flight.flight(id);


--
-- Name: seat_availability fkdbluistflbigkijy1qy8udnnm; Type: FK CONSTRAINT; Schema: flight; Owner: -
--

ALTER TABLE ONLY flight.seat_availability
    ADD CONSTRAINT fkdbluistflbigkijy1qy8udnnm FOREIGN KEY (flight_id) REFERENCES flight.flight(id);


--
-- Name: seat_availability fkdhqds9f83ht8ynrd0xrq8go2i; Type: FK CONSTRAINT; Schema: flight; Owner: -
--

ALTER TABLE ONLY flight.seat_availability
    ADD CONSTRAINT fkdhqds9f83ht8ynrd0xrq8go2i FOREIGN KEY (seat_id) REFERENCES flight.seat(id);


--
-- Name: route fkfgy0gl8x94s6jf9pw5irxhjax; Type: FK CONSTRAINT; Schema: flight; Owner: -
--

ALTER TABLE ONLY flight.route
    ADD CONSTRAINT fkfgy0gl8x94s6jf9pw5irxhjax FOREIGN KEY (arrival_airport_id) REFERENCES flight.airport(id);


--
-- Name: flight fkmofq89ullrd4qk1hllnyf8pn5; Type: FK CONSTRAINT; Schema: flight; Owner: -
--

ALTER TABLE ONLY flight.flight
    ADD CONSTRAINT fkmofq89ullrd4qk1hllnyf8pn5 FOREIGN KEY (aircraft_id) REFERENCES flight.aircraft(id);


--
-- Name: route fkrhi4sk6rhj303om7ur6wp1g4q; Type: FK CONSTRAINT; Schema: flight; Owner: -
--

ALTER TABLE ONLY flight.route
    ADD CONSTRAINT fkrhi4sk6rhj303om7ur6wp1g4q FOREIGN KEY (departure_airport_id) REFERENCES flight.airport(id);