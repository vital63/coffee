--
-- Изменяйте типы для тестовой БД.
-- Для MySQL юзайте InnoDB
--
-- !!!: Сохраняйте регистр в мэппингах для Hibernate (на mysql под linux это важно).
--

--
-- Сорт кофе
--
create table CoffeeType (
  id int not null, -- pk
  type_name varchar(200) not null, -- название
  price double not null, -- цена
  disabled char(1), -- если disabled = 'Y', то не показывать данный сорт в списке доступных сортов
  primary key (id)
) type=InnoDB;

create index CT_I on CoffeeType (
  id asc
);

--
-- Перевод сортов кофе на русский язык
--
create table CoffeeTypeTranslate_Ru (
  id int not null, -- pk
  type_name varchar(200) not null, -- название
  primary key (id)
) type=InnoDB;

create index CTT_I on CoffeeType (
  id asc
);

--
-- Заказ
--
create table CoffeeOrder (
  id int not null, -- pk
  order_date datetime not null, -- время заказа
  name varchar(100), -- имя заказчика
  delivery_address varchar(200) not null, -- куда доставлять
  cost double, -- сколко стоит (алгоритм подсчёта может поменяться, поэтому надо хранить стоимость)
  primary key (id)
) type=InnoDB;

create index CO_I1 on CoffeeOrder (
  id asc
);

--
-- Одна позиция заказа
--
create table CoffeeOrderItem (
  id int not null, -- pk
  type_id int not null, -- сорт кофе
  order_id int not null, -- к какому заказу принадлежит
  quantity int, -- сколько чашек
  primary key (id)
) type=InnoDB;

create index COI_I on CoffeeOrderItem (
  order_id asc
);

create index COI_3 on CoffeeOrderItem (
  type_id asc
);

alter table CoffeeOrderItem
  add constraint COI_CO foreign key (order_id)
    references CoffeeOrder (id);


alter table CoffeeOrderItem
  add constraint COI_CT foreign key (type_id)
    references CoffeeType (id);

--
-- Конфигурация
--
create table Configuration (
  id varchar(20) not null, -- pk, название свойства
  value varchar(30), -- значение
  primary key (id)
) type=InnoDB;

--
--Процедура для рассчета стоимости заказа
--

DELIMITER $$

CREATE DEFINER=`root`@`localhost` PROCEDURE `calculate_cost_order`(OUT coffee FLOAT, OUT delivery FLOAT)
BEGIN
	declare type_id_, count_, finish int;
	declare price_ float;
	declare n_, discount int;
    declare m_, x_ float;

	declare cur cursor for select type_id, count from coffee_count; 
    declare continue handler for not found set finish = 1; 

	select cast(value as signed) into n_ from configuration where id='n';
	select cast(value as signed) into m_ from configuration where id='m';
	select cast(value as signed) into x_ from configuration where id='x';

    open cur; 
    set finish = 0; 

	while finish = 0 do
		fetch cur into type_id_, count_;
		select price into price_ from coffeetype where id = type_id_;
		set discount = floor(count_ / n_);
		update coffee_count set cost = (count_ - discount) * price_ where type_id = type_id_;
    end while; 
    close cur;

	select sum(cost) into coffee from coffee_count;

    if coffee > x_ then
        set delivery = 0;
    else
        set delivery = m_;
    end if;
END
