# lmlDBMS 用java实现的一款关系型数据库

### 实现功能

	1.创建用户
		create user userName password;  例如：create user ming 1234;
		(1) 用户信息通过以对象序列化方式存放
	
	2.登录验证
		mysql -u userName -p password;  例如：mysql -u ming -p 1234;
		(1) 判断是否有相应的对象信息文件
	
	3.删除用户
		drop user userName;  例如：drop user ming;
		(1) 递归的删除该用户的所有文件
	
	4.创建数据库
		create database databaseName;  例如：create database test;
		(1) 在数据库总目录下创建相应的文件夹
	
	5.删除数据库
		drop database databaseName;  例如：drop database test;
		(1) 在数据库总目录下递归的删除该文件夹
	
	6.选择数据库
		use databaseName;  例如：use test;
		(1) 指明当前所选择的数据库
		(2) 在执行和表相关的操作的时候，需要确保已经选择了数据库
	
	7.权限控制
		grant authType to userName;  例如：grant all to ming;
		(1) 只实现了三种权限：all(1所有权限)、 insert(2可添加数据)、 select(3可查看)
		(2) all权限用户才能赋予权限
	
	8.显示创建的数据库/表
		show databases/tables;  例如：show databases;
		(1) 显示所有的以创建的数据库名或者表名
	
	9.创建表
		create table tableName(fieldName typeName);  
				例如：create table stu(id int, name varchar)；
		(1) 支持三种类型数据：int double varchar(字符型)
		(2) 数据文件和字典文件均采用字符型，可直接阅读，按行存储
		(3) 数据文件放在data目录，一个文件为一组，存储固定行数的记录(行数可更改)
		(4) 字典文件存储属性相关信息
		(5) 初始化索引文件
	
	10.删除表
		drop table tableName;  例如：drop table stu;
		(1) 递归删除该表所有文件
	
	11.查看表信息
		desc tableName;  例如：desc stu;
		(1) 通过字典文件，输出表的相关信息，便于插入数据
	
	12.插入
		insert into table[(fieldName)] values(value);
			例如：insert into table(id, name) values(1, liming);
		(1) 实现指定属性插入数据
		(2) 实现不指定属性，插入数据，此时的默认属性顺序为定义时的顺序
		(3) 每次添加数据需要维护索引文件
	
	13.索引
		(1) 为关系表中的所有属性建立稠密索引(文件组名)
		(2) 对于数值类型的属性，采用TreeMap(红黑树实现)
		(3) 对于字符类型的属性，采用HashMap
		(4) 在删除、更新、查看操作中，通过索引快速得到满足条件的文件组名集合，提高查询效率
		(5) 文件组的大小通过groupMaxNum控制
	
	14.删除
		delete from tableName [where子句];  例如：delete from stu where id < 10;
		(1) 如果指定条件，则删除对应的元组
		(2) 没有指定条件，删除该表所有数据
		(3) 维护索引
	
	15.更新
		update tableName set fieldName = value [where子句];
			例如：update stu set name = edwin where id > 10 and name = liming;
		(1) 如果指定条件，则更新对应的元组
		(2) 没有指定条件，更新该表所有数据
		(3) 维护索引
	
	16.查询
		select *|field from tableName[,tableName] [where子句];
			例如：select stu.name, class.name from stu,class where stu.Cid = class.id and stu.name = liming;
															
		(1) 实现可选投影查询，可以选出自己想要的任意表的属性，*表示所有属性
		(2) 实现多关系等值连接查询，需在where子句中给出连接的条件
		(3) 实现and多条件查询，可以通过and来增加查询条件
		(4) 若没有查询条件，则查询得到该表所有数据
	
	17.查询优化
		(1) 实现启发式关系代数优化，在语法树中，优先进行选择投影操作，然后才进行连接操作



### 使用方法
	运行Start文件即可，然后就可以开始操作
	默认会创建root用户，密码为123456

## 增加

增加标签v1.4

## 分支test1