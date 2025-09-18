# Dockerfile para Codespace Java
FROM eclipse-temurin:17-jdk

# Diretório de trabalho
WORKDIR /workspace

# Copia todos os arquivos do projeto
COPY . /workspace

# Instalações adicionais (se necessário)
# RUN apt-get update && apt-get install -y maven

# Comando padrão (pode ser ajustado)
CMD ["bash"]
