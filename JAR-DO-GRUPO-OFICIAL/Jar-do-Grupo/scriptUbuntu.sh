#!/bin/bash

echo -e "FÁRMACOS"
echo -e "Olá, serei seu assistente para instalação!"
echo -e "Verificando se você possui o Docker e o Java instalado na sua máquina!"
sleep 1

java -version
# Verifica se o Java está instalado
echo -e "Baixando o arquivo java..."
sudo apt install openjdk-17-jdk -y
echo -e "java baixado o arquivo java..."
sleep 5
# Atualiza a lista de pacotes
sudo apt update && sudo apt upgrade -y

# Instala o Docker
sudo apt install docker.io -y
sleep 2
# Exibe a versão do Docker instalada
docker --version
sleep 4
# Exibe mensagem indicando que o Docker foi instalado com sucesso
echo "Docker foi instalado com sucesso."

# Ativando o Docker no sistema operacional
sudo systemctl start docker

# Habilita o serviço do Docker para ser iniciado junto ao sistema operacional
sudo systemctl enable docker

# Pull da imagem do MySQL 5.7
sudo docker pull mysql:5.7

# Exibe mensagem indicando que o MySQL foi instalado com sucesso
sudo docker images

# Cria um contêiner Docker com o MySQL Workbench
sudo docker run -d -p 3306:3306 --name ContainerBdFarmacos -e "MYSQL_DATABASE=farmacos" -e "MYSQL_ROOT_PASSWORD=urubu100" mysql:5.7

# Confirma a criação do contêiner
sudo docker ps -a

# Para acessar o contêiner e manipular o banco de dados
sudo docker exec -it ContainerBdFarmacos bash <<EOF
# Entrar com senha do root
mysql -u root -p <<MYSQL_SCRIPT
# Criação do banco e das tabelas usando o utilitário mysql
CREATE DATABASE farmacos;
USE farmacos;

CREATE TABLE AME (
idAme INT PRIMARY KEY AUTO_INCREMENT,
nomeAme VARCHAR(45),
cep CHAR(9)
) AUTO_INCREMENT = 1000;

CREATE TABLE maquina (
idMaquina VARCHAR(100) PRIMARY KEY,
hostName VARCHAR(100),
sistemaOperacional VARCHAR (45),
arquitetura INT,
fabricante VARCHAR (45),
tempoAtividade VARCHAR(45),
fkAme INT, CONSTRAINT FK_Ame FOREIGN KEY (fkAme) REFERENCES AME(idAme)
);

CREATE TABLE permissao (
idPermissao INT PRIMARY KEY AUTO_INCREMENT,
tipoPermissao VARCHAR (45)
);

CREATE TABLE usuario (
idUsuario INT PRIMARY KEY AUTO_INCREMENT,
nome varchar(100),
email VARCHAR (100),
senha VARCHAR (45),
cargo VARCHAR (45),
fkAme INT, CONSTRAINT FK_AmeUsuario FOREIGN KEY (fkAme) REFERENCES AME(idAme),
fkPermissaoUsuario INT, CONSTRAINT FK_perm FOREIGN KEY (fkPermissaoUsuario) REFERENCES permissao(idPermissao)
);

CREATE TABLE tipoComponente (
idTipoComp INT PRIMARY KEY AUTO_INCREMENT,
nomeTipoComp VARCHAR (45)
);

CREATE TABLE parametro (
idParametro INT primary key AUTO_INCREMENT,
maximo INT,
medio INT,
fkPermissaoParametro INT, CONSTRAINT FkPerm_param FOREIGN KEY (fkPermissaoParametro) REFERENCES permissao(idPermissao),
fkTipoComponente INT, CONSTRAINT FkTipo_comp foreign key (fkTipoComponente) references tipoComponente(idTipoComp)
);

CREATE TABLE maquinaTipoComponente (
idMaqTipoComp INT PRIMARY KEY AUTO_INCREMENT,
fkMaquina VARCHAR(100), CONSTRAINT FK_Maquina FOREIGN KEY (fkMaquina) REFERENCES maquina(idMaquina),
fkTipoComp INT, CONSTRAINT FK_TipoComp FOREIGN KEY (fkTipoComp) REFERENCES tipoComponente(idTipoComp),
numProcesLogicos INT,
numProcesFisicos INT,
tamanhoTotalRam INT,
tamanhoTotalDisco INT,
enderecoMac VARCHAR (45),
numSerial VARCHAR(45),
ipv4 VARCHAR (45)
);

CREATE TABLE dadosComponente (
idDadosComponentes INT PRIMARY KEY AUTO_INCREMENT,
fkMaquina VARCHAR(100), CONSTRAINT Dados_FK_Maquina FOREIGN KEY (fkMaquina) REFERENCES maquina(idMaquina),
fkTipoComponente INT, CONSTRAINT Dados_FK_TipoComp FOREIGN KEY (fkTipoComponente) REFERENCES tipoComponente(idTipoComp),
fkMaquinaTipoComponente INT, CONSTRAINT Dados_FK_MaqTipoComp FOREIGN KEY (fkMaquinaTipoComponente) REFERENCES maquinaTipoComponente(idMaqTipoComp),
qtdUsoCpu DECIMAL (4, 2),
memoriaEmUso DECIMAL (2, 1),
memoriaDisponivel DECIMAL (2, 1),
usoAtualDisco INT,
usoDisponivelDisco INT,
bytesRecebido float,
bytesEnviado float,
dtHora datetime DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO AME (idAme, nomeAme, cep) VALUES 
(NULL, 'AME Campinas', '13087-535'),
(NULL, 'AME Guarulhos', '07034-911');

INSERT INTO permissao (tipoPermissao) VALUES ('NOC');
INSERT INTO permissao (tipoPermissao) VALUES ('Visualização');

INSERT INTO tipoComponente (nomeTipoComp) VALUES ('CPU');
INSERT INTO tipoComponente (nomeTipoComp) VALUES ('RAM');
INSERT INTO tipoComponente (nomeTipoComp) VALUES ('DISCO');
INSERT INTO tipoComponente (nomeTipoComp) VALUES ('REDE');

SELECT dc1.memoriaEmUso, dc2.qtdUsoCpu, TIME_FORMAT(dc1.dtHora, '%H:%i:%s') AS hora_formatada
FROM dadosComponente dc1
LEFT JOIN dadosComponente dc2 ON dc1.dtHora = dc2.dtHora
WHERE dc1.memoriaEmUso IS NOT NULL AND dc2.qtdUsoCpu IS NOT NULL
ORDER BY dc1.dtHora DESC;

SELECT hostName, avg(bytesRecebido) AS medBytesRecebido, avg(bytesEnviado) AS medBytesEnviado FROM dadosComponente join maquina on idMaquina = fkMaquina GROUP BY hostName;
SELECT hostName, avg(usoAtualDisco) AS medUsoAtualDisco, avg(usoDisponivelDisco) AS medUsoDisponivelDisco FROM dadosComponente join maquina on idMaquina = fkMaquina GROUP BY hostName;
SELECT hostName, avg(qtdUsoCpu) AS medUsoAtualCpu FROM dadosComponente join maquina on idMaquina = fkMaquina GROUP BY hostName;
SELECT hostName, avg(memoriaEmUso) AS medMemoriaEmUso, avg(memoriaDisponivel) AS medMemoriaDisponivel FROM dadosComponente join maquina on idMaquina = fkMaquina GROUP BY hostName order by medMemoriaEmUso desc;
MYSQL_SCRIPT
EOF

curl -LJO https://github.com/Sprints-2-semestre/Jar-do-Grupo/raw/main/looca-oficial/target/looca-oficial-1.0-jar-with-dependencies.jar
if [ $? -eq 0 ]; then
    # Verificando se o arquivo baixado é um arquivo .jar válido
    if [ -f looca-oficial-1.0-jar-with-dependencies.jar ]; then
        echo ""
        echo "Iniciando o software"
        sleep 1
        echo "Bem-Vindo a Fármacos"
        echo ""
        java -jar looca-oficial-1.0-jar-with-dependencies.jar
    else
        echo "Erro ao rodar o .jar"
        exit 1
    fi
else
    echo "Erro ao executar o curl"
    exit 1
fi