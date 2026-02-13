create table if not exists usuarios (
  login varchar(100) primary key,
  senha_hash varchar(120) not null,
  tipo_usuario varchar(30) not null
);
