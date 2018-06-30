# Mim2Mim - Desktop (m2m)

Este projeto se encontra em atualização

### Download

Para baixar o último lançamento da aplicação, [clique aqui](https://github.com/henrique-dev/m2m-Desktop/raw/master/dist/FacilTransferencia.jar).

Não possui instalador, é só baixar e executar.

Obs.: Seu computador deve possuir o [java](https://java.com/pt_BR/download/) instalado.

### Funções atuais
- [X] Transferência de arquivos para o smartphone.
- [X] Pareamento automático entre smartphone e computador.
- [X] Tamanho máximo do arquivo para envio: 30Mb.

### Proximas funções
- [ ] Recebimento de arquivos do smartphone.
- [ ] Pareamente manual entre smartphone e computador.
- [ ] Aumento do tamanho máximo de envio e recebimento de arquivos para 200Mb.
- [ ] Transferência a mais de um dispositivo.

### Descrição

Esta aplicação funciona em par com [m2m-Mobile](https://github.com/henrique-dev/m2m-Mobile). Essa versão é a desktop e
tem como objetivo a transferência de arquivos de um computador para um smartphone através de conexão Wireless.

### Funcionamento

Ambos os dispositivos tem que estar na mesma rede. A versão desktop possui um receptor de broadcast que fica na escuta de mensagens, que são enviadas pela aplicação do smartphone. Por sua vez, o smartphone possui um transmissor de broadcast que envia um determinado número de broadcasts na rede, e simultaneamente abre uma entrada de requisições TCP por 5 segundos. 

Quando o desktop recebe uma mensagem de broadcast, o mesmo envia uma requisição de conexão através do protocolo TCP para o smartphone, e assim que o smartphone aceita, ambas as aplicações ficam pareadas e prontas para transferência de dados.

Desta forma, o conhecimento de ambas na rede é feito automaticamente.

### Uso

Esta é a janela única da aplicação:

![](/rd/desktop.png)

Na área **Dispositivos conectados** se encontra a lista de smartphones conectados no momento na aplicação (por enquanto só é possível a transferência a somente um dispositivo, por mais que possam haver mais conectados).

Na área **Arquivos** se encontra a lista de arquivos a serem transferidos ou que já foram transferidos (o status é transcrito no mesmo na lista).

E por fim, na área **Arraste os arquivos para cá** é onde se deve soltar os arquivos arrastados com o mouse. Os arquivos são adicionados automaticamente para transferência.
