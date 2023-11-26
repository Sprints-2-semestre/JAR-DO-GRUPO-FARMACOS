#!/bin/bash

VERSAO=17
echo -e "FÁRMACOS"
echo -e "Olá usuário, serei seu assistente para instalação do Java!"
sleep 1
echo -e "Verificando se você possui o Java instalado na sua máquina!"
sleep 1

# Verifica se o Java está instalado
if java -version &> /dev/null; then
    echo -e "Você já possui o Java instalado na sua máquina!"
    echo -e "Vamos atualizar os pacotes..."
    sleep 1
    sudo apt update && sudo apt upgrade -y
    echo -e "Pacotes atualizados!"
else
    echo -e "Não foi encontrada nenhuma versão do Java na sua máquina, mas iremos resolver isso!"
    echo -e "Você deseja instalar o Java na sua máquina (S/N)?"
    read inst

    if [ "$inst" == "S" ] || [ "$inst" == "s" ]; then
        echo -e "Ok! Você decidiu instalar o Java na sua máquina."

        # Instala o Java
        sudo apt install openjdk-$VERSAO-jre
        echo -e "Java instalado com sucesso!"
    else
        echo -e "Entendido, você optou por não instalar o Java. Até a próxima!"
        exit 1
    fi
fi

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