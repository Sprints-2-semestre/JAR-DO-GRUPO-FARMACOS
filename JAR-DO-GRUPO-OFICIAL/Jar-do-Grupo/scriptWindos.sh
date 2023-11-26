#!/bin/bash

echo -e "FÁRMACOS"
echo -e "Olá, serei seu assistente para instalação!"
echo -e "Verificando se você possui o Docker instalado na sua máquina!"
sleep 1

# Verifica se o script está sendo executado como root
if [ "$EUID" -ne 0 ]; then
  echo "Por favor, execute este script como root."
  exit 1
fi
# Atualiza a lista de pacotes
apt update

# Instala as dependências necessárias para o Docker
apt install -y \
    apt-transport-https \
    ca-certificates \
    curl \
    software-properties-common

# Baixa a chave GPG oficial do Docker
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | gpg --dearmor -o /usr/share/keyrings/docker-archive-keyring.gpg

# Adiciona o repositório estável do Docker ao sistema
echo "deb [arch=amd64 signed-by=/usr/share/keyrings/docker-archive-keyring.gpg] https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable" | tee /etc/apt/sources.list.d/docker.list > /dev/null

# Atualiza novamente a lista de pacotes para incluir o repositório do Docker
apt update

# Instala o Docker
apt install -y docker-ce docker-ce-cli containerd.io

# Verifica se a instalação do Docker foi bem-sucedida
if [ $? -ne 0 ]; then
  echo "Erro ao instalar o Docker. Verifique a instalação e tente novamente."
  exit 1
fi

# Exibe a versão do Docker instalada
docker --version

# Exibe mensagem indicando que o Docker foi instalado com sucesso
echo "Docker foi instalado com sucesso."

# Atualiza todos os pacotes do sistema
apt upgrade -y

# Exibe mensagem indicando que os pacotes do sistema foram atualizados
echo "Pacotes do sistema foram atualizados."

# Baixa a imagem do SQL Server do Docker Hub
docker pull mcr.microsoft.com/mssql/server

# Configuração do SQL Server em um contêiner Docker
docker run -e 'ACCEPT_EULA=Y' -e 'SA_PASSWORD=urubu100' -p 1433:1433 --name containerBdSqlServerFarmacos -d mcr.microsoft.com/mssql/server

# Verifica se o contêiner foi criado com sucesso
if [ $? -ne 0 ]; then
  echo "Erro ao configurar o SQL Server em um contêiner Docker. Verifique a instalação e tente novamente."
  exit 1
fi
# Exibe mensagem indicando que o SQL Server foi configurado
echo "SQL Server foi configurado em um contêiner Docker."

# Exibe informações sobre o contêiner
docker ps -a | grep containerBdSqlServerFarmacos

echo -e "Verificando se você possui o Java instalado na sua máquina!"
sleep 1

# Comandos SQL
docker exec -i containerBdSqlServerFarmacos /opt/mssql-tools/bin/sqlcmd -S localhost -U SA -P 'urubu100' -d farmacos -i - <<EOF
-- Seus comandos SQL aqui
-- Criação do banco de dados
USE master;
GO

IF EXISTS(SELECT 1 FROM sys.databases WHERE name = 'farmacos')
    DROP DATABASE farmacos;
GO

CREATE DATABASE farmacos;
GO

USE farmacos;
GO

CREATE TABLE AME (
    idAme INT PRIMARY KEY IDENTITY(1000,1),
    nomeAme VARCHAR(45),
    cep CHAR(9)
);

CREATE TABLE maquina (
    idMaquina VARCHAR(100) PRIMARY KEY,
    hostName VARCHAR(100),
    sistemaOperacional VARCHAR(45),
    arquitetura INT,
    fabricante VARCHAR(45),
    tempoAtividade VARCHAR(45),
    fkAme INT,
    CONSTRAINT FK_Ame FOREIGN KEY (fkAme) REFERENCES AME(idAme)
);

CREATE TABLE permissao (
    idPermissao INT PRIMARY KEY IDENTITY(1,1),
    tipoPermissao VARCHAR(45)
);

CREATE TABLE usuario (
    idUsuario INT PRIMARY KEY IDENTITY(1,1),
    nome VARCHAR(100),
    email VARCHAR(100),
    senha VARCHAR(45),
    cargo VARCHAR(45),
    fkAme INT,
    CONSTRAINT FK_AmeUsuario FOREIGN KEY (fkAme) REFERENCES AME(idAme),
    fkPermissaoUsuario INT,
    CONSTRAINT FK_perm FOREIGN KEY (fkPermissaoUsuario) REFERENCES permissao(idPermissao)
);

CREATE TABLE tipoComponente (
    idTipoComp INT PRIMARY KEY IDENTITY(1,1),
    nomeTipoComp VARCHAR(45)
);

CREATE TABLE parametro (
    idParametro INT PRIMARY KEY IDENTITY(1,1),
    maximo INT,
    medio INT,
    fkPermissaoParametro INT,
    CONSTRAINT FkPerm_param FOREIGN KEY (fkPermissaoParametro) REFERENCES permissao(idPermissao),
    fkTipoComponente INT,
    CONSTRAINT FkTipo_comp FOREIGN KEY (fkTipoComponente) REFERENCES tipoComponente(idTipoComp)
);

CREATE TABLE maquinaTipoComponente (
    idMaqTipoComp INT PRIMARY KEY IDENTITY(1,1),
    fkMaquina VARCHAR(100),
    CONSTRAINT FK_Maquina FOREIGN KEY (fkMaquina) REFERENCES maquina(idMaquina),
    fkTipoComp INT,
    CONSTRAINT FK_TipoComp FOREIGN KEY (fkTipoComp) REFERENCES tipoComponente(idTipoComp),
    numProcesLogicos INT,
    numProcesFisicos INT,
    tamanhoTotalRam INT,
    tamanhoTotalDisco INT,
    enderecoMac VARCHAR(45),
    numSerial VARCHAR(45),
    ipv4 VARCHAR(45)
);

CREATE TABLE dadosComponente (
    idDadosComponentes INT PRIMARY KEY IDENTITY(1,1),
    fkMaquina VARCHAR(100),
    CONSTRAINT Dados_FK_Maquina FOREIGN KEY (fkMaquina) REFERENCES maquina(idMaquina),
    fkTipoComponente INT,
    CONSTRAINT Dados_FK_TipoComp FOREIGN KEY (fkTipoComponente) REFERENCES tipoComponente(idTipoComp),
    fkMaquinaTipoComponente INT,
    CONSTRAINT Dados_FK_MaqTipoComp FOREIGN KEY (fkMaquinaTipoComponente) REFERENCES maquinaTipoComponente(idMaqTipoComp),
    qtdUsoCpu DECIMAL(4, 2),
    memoriaEmUso DECIMAL(3, 1),
    memoriaDisponivel DECIMAL(3, 1),
    usoAtualDisco INT,
    usoDisponivelDisco INT,
    bytesRecebido FLOAT,
    bytesEnviado FLOAT,
    dtHora DATETIME DEFAULT GETDATE()
);

INSERT INTO AME (nomeAme, cep) VALUES 
('AME Campinas', '13087-535'),
('AME Guarulhos', '07034-911');

INSERT INTO permissao (tipoPermissao) VALUES ('NOC');
INSERT INTO permissao (tipoPermissao) VALUES ('Visualização');

INSERT INTO tipoComponente (nomeTipoComp) VALUES ('CPU');
INSERT INTO tipoComponente (nomeTipoComp) VALUES ('RAM');
INSERT INTO tipoComponente (nomeTipoComp) VALUES ('DISCO');
INSERT INTO tipoComponente (nomeTipoComp) VALUES('REDE');

EOF
