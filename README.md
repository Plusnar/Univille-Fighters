Univille Fighters

Univille Fighters é um jogo de luta 2D desenvolvido para o projeto final de Programação Orientada a objetos I do professor PHD Jackson Antonio do Prado Lima.
O projeto simula batalhas entre personagens em diferentes arenas, com sistema de rounds, 
seleção de personagens, trilha sonora, e telas de menu, tutorial e créditos.

Estrutura do Projeto
src/main/java/com/luta2d/: Código-fonte principal.

Main.java: Ponto de entrada do jogo, inicializa a janela principal.
GameWindow.java: Gerencia a janela do jogo, alternando entre diferentes telas (menu, seleção, luta, tutorial, créditos).
MenuScreen.java: Tela inicial com opções para jogar, tutorial e créditos.
CharacterSelectionScreen.java: Tela para seleção dos personagens pelos jogadores.
ArenaScreen.java: Tela de luta, gerencia o combate, rounds, e exibe o fundo da arena.
Player.java: Lógica dos jogadores, incluindo movimentação, ataques, bloqueios e interação com o personagem selecionado.
Character.java: Representa os personagens jogáveis, com atributos como vida, dano, velocidade, etc.
AudioManager.java: Gerencia músicas e efeitos sonoros do jogo.
TutorialScreen.java: Tela de tutorial com instruções de jogo.
CreditsScreen.java: Tela de créditos, exibe vídeo e mensagem.
VideoPlayer.java: Utilitário para reprodução de vídeos nos créditos.
src/main/resources/: Recursos do jogo.

audio/: Arquivos de música e efeitos sonoros.
images/: Imagens dos personagens, arenas, efeitos e telas.
videos/: Vídeos utilizados nos créditos e introdução.
test/: Testes automatizados para as principais classes do jogo.

Funcionalidades
Menu interativo: Permite iniciar o jogo, acessar tutorial ou créditos.
Seleção de personagens: Jogadores escolhem entre diferentes lutadores, cada um com atributos únicos.
Sistema de combate: Luta em arenas com rounds, contagem de vitórias, ataques normais e especiais, bloqueio e movimentação.
Trilha sonora: Músicas temáticas para menu, seleção, arenas e tutorial.
Tutorial: Explica controles e regras do jogo.
Créditos: Exibe vídeo ou mensagem com os responsáveis pelo projeto.
Como Executar
Certifique-se de ter o Java instalado.
Compile o projeto usando Maven ou seu IDE.
Execute a classe Main.java para iniciar o jogo.
Dependências
Java 8+
Swing (biblioteca padrão)
Maven para gerenciamento de dependências e build.
Testes
Os testes automatizados estão localizados em test/java/com/luta2d/ e cobrem as principais funcionalidades das classes do jogo.

Observações:
O projeto utiliza recursos multimídia (áudio, imagens, vídeo) que devem estar presentes nas pastas indicadas.
O código está estruturado para facilitar a adição de novos personagens, arenas e funcionalidades a quem se interessar.
