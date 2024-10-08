import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.Random;

public class PongGame extends JPanel implements KeyListener, ActionListener {
    private int paddleWidth = 10;
    private int paddle1Height = 120;
    private int paddle2Height = 120;
    private int paddle1Y = 150, paddle2Y = 150;
    private int ballX = 250, ballY = 150, ballDiameter = 50;
    private int ballXSpeed = 5, ballYSpeed = 5;
    private int ScoreP1 = 0;
    private int ScoreP2 = 0;
    private int BonusType; 

    private final int MAX_SCORE = 10;  // Limite du score

    // Bonus/Malus
    private int bonusX = -100, bonusY = -100, bonusWidth = 100, bonusHeight = 100;
    private boolean bonusActive = false;
    private boolean isBonus = true;  // Bonus (vert) ou malus (rouge)

    private Timer timer;
    private Timer paddle1MoveTimer;
    private Timer paddle2MoveTimer;
    private Timer bonusTimer;
    private Timer malusTimer;  // Timer pour la durée du malus

    private boolean paddle1MovingUp = false, paddle1MovingDown = false;
    private boolean paddle2MovingUp = false, paddle2MovingDown = false;

    private Random random;

    // Variable pour suivre quel paddle a touché la balle en dernier
    private boolean lastHitByPaddle1 = false; 

    // Variables pour les sons
    private SoundPlayer paddleHitSound;
    private SoundPlayer bonusSound;
    private SoundPlayer malusSound;
    private SoundPlayer ambianceSound;
    private boolean controlsInvertedPaddle1 = false; // Inversion des contrôles du paddle 1
    private boolean controlsInvertedPaddle2 = false; // Inversion des contrôles du paddle 2

    public PongGame() {
        this.setPreferredSize(new Dimension(500, 300));
        this.setBackground(Color.BLACK);
        this.setFocusable(true);
        this.addKeyListener(this);
        timer = new Timer(10, this);
        timer.start();

        paddle1MoveTimer = new Timer(10, evt -> movePaddle1());
        paddle2MoveTimer = new Timer(10, evt -> movePaddle2());

        random = new Random();

        // Timer pour générer des bonus/malus toutes les 7 secondes
        bonusTimer = new Timer(7000, evt -> spawnBonus());
        bonusTimer.start();

        // Charger les sons
        paddleHitSound = new SoundPlayer("paddle.wav");
        bonusSound = new SoundPlayer("bonus.wav");
        malusSound = new SoundPlayer("malus.wav");
        ambianceSound = new SoundPlayer("ambiance.wav");

        // Jouer le son d'ambiance en boucle
        ambianceSound.loop();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(Color.WHITE);
        
        // Afficher les paddles
        g.fillRect(0, paddle1Y, paddleWidth, paddle1Height);
        g.fillRect(getWidth() - paddleWidth, paddle2Y, paddleWidth, paddle2Height);
        
        // Afficher la balle
        g.fillOval(ballX, ballY, ballDiameter, ballDiameter);
        
        // Afficher les scores
        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.drawString("Player 1: " + ScoreP1, 50, 30);
        g.drawString("Player 2: " + ScoreP2, getWidth() - 150, 30);
        
        // Dessiner le bonus/malus s'il est actif
        if (bonusActive) {
            g.setColor(isBonus ? Color.GREEN : Color.RED);
            g.fillRect(bonusX, bonusY, bonusWidth, bonusHeight);
        }
    }

    public void actionPerformed(ActionEvent e) {
        if (ScoreP1 >= MAX_SCORE || ScoreP2 >= MAX_SCORE) {
            endGame();
            return;
        }
        
        ballX += ballXSpeed;
        ballY += ballYSpeed;
    
        if (ballY <= 0 || ballY >= getHeight() - ballDiameter) {
            ballYSpeed = -ballYSpeed;
        }
    
        // Paddle 1 touche la balle
        if (ballX <= paddleWidth && ballY + ballDiameter >= paddle1Y && ballY <= paddle1Y + paddle1Height) {
            ballXSpeed = -ballXSpeed;
        
            paddleHitSound.play();  // Jouer le son quand la balle touche le paddle 1
        }
    
        // Paddle 2 touche la balle
        if (ballX >= getWidth() - paddleWidth - ballDiameter && ballY + ballDiameter >= paddle2Y && ballY <= paddle2Y + paddle2Height) {
            ballXSpeed = -ballXSpeed;
           
            paddleHitSound.play();  // Jouer le son quand la balle touche le paddle 2
        }
    
        // Gérer les sorties de balle
        if (ballX < 0 || ballX > getWidth()) {
            scoreP();  // Mettre à jour les scores lorsque la balle sort
        }
    
        // Gérer la collision de la balle avec le bonus/malus
        if (bonusActive && ballX + ballDiameter >= bonusX && ballX <= bonusX + bonusWidth &&
            ballY + ballDiameter >= bonusY && ballY <= bonusY + bonusHeight) {
            applyBonusOrMalus();
            bonusActive = false;
        }
    
        repaint();
    }

    // Méthode pour déplacer la paddle 1 en fonction de la direction
    private void movePaddle1() {
        if (controlsInvertedPaddle1) {  // Vérifier si les contrôles sont inversés
            if (paddle1MovingDown && paddle1Y > 0) {
                paddle1Y -= 5;  // Inverser le mouvement
            }
            if (paddle1MovingUp && paddle1Y < getHeight() - paddle1Height) {
                paddle1Y += 5;  // Inverser le mouvement
            }
        } else {
            if (paddle1MovingUp && paddle1Y > 0) {
                paddle1Y -= 5;
            }
            if (paddle1MovingDown && paddle1Y < getHeight() - paddle1Height) {
                paddle1Y += 5;
            }
        }
    }

    // Méthode pour déplacer la paddle 2 en fonction de la direction
    private void movePaddle2() {
        if (controlsInvertedPaddle2) {  // Vérifier si les contrôles sont inversés
            if (paddle2MovingDown && paddle2Y > 0) {
                paddle2Y -= 5;  // Inverser le mouvement
            }
            if (paddle2MovingUp && paddle2Y < getHeight() - paddle2Height) {
                paddle2Y += 5;  // Inverser le mouvement
            }
        } else {
            if (paddle2MovingUp && paddle2Y > 0) {
                paddle2Y -= 5;
            }
            if (paddle2MovingDown && paddle2Y < getHeight() - paddle2Height) {
                paddle2Y += 5;
            }
        }
    }

    public void scoreP() {
        if (ballX < 0) {
            ScoreP2 += 1;
        } else if (ballX > getWidth()) {
            ScoreP1 += 1;
        }
        
        ballX = getWidth() / 2 - ballDiameter / 2;
        ballY = getHeight() / 2 - ballDiameter / 2;
        ballXSpeed = 5;
        ballYSpeed = 5;
    }

    public void spawnBonus() {
        bonusX = random.nextInt(getWidth() - bonusWidth);
        bonusY = random.nextInt(getHeight() - bonusHeight);
        BonusType = random.nextInt(5);
        isBonus = random.nextBoolean();  // Choisir si c'est un bonus ou un malus
        bonusActive = true;  // Activer le bonus/malus
        isBonus = random.nextBoolean();
        bonusActive = true;
    }

    // Appliquer les effets du bonus ou malus
    public void applyBonusOrMalus() {
        switch (BonusType) {
            case 1:
                ballXSpeed += (isBonus ? 5 : -2);  // Bonus : augmenter la vitesse, Malus : la diminuer
                ballYSpeed += (isBonus ? 5 : -2);
                break;
            case 2:
                if (lastHitByPaddle1) {
                    controlsInvertedPaddle1 = !isBonus;  // Inverser les contrôles du joueur 1
                    if (!isBonus) { // Si c'est un malus
                        startMalusTimer(1);  // Démarrer le timer de malus pour le joueur 1
                    }
                } else {
                    controlsInvertedPaddle2 = !isBonus;  // Inverser les contrôles du joueur 2
                    if (!isBonus) { // Si c'est un malus
                        startMalusTimer(2);  // Démarrer le timer de malus pour le joueur 2
                    }
                }
                break;
            // Ajoutez d'autres types de bonus/malus ici si nécessaire
            case 3:
                if (lastHitByPaddle1) {
                    paddle1Height = Math.max(60, paddle1Height - 20); // Réduire la taille du paddle 1
                } else {
                    paddle2Height = Math.max(60, paddle2Height - 20); // Réduire la taille du paddle 2
                }
        }
    }

    // Méthode pour démarrer le timer de malus
    private void startMalusTimer(int player) {
        malusTimer = new Timer(10000, evt -> {
            if (player == 1) {
                controlsInvertedPaddle1 = false;  // Rétablir les contrôles du joueur 1
            } else {
                controlsInvertedPaddle2 = false;  // Rétablir les contrôles du joueur 2
            }
            malusTimer.stop();  // Arrêter le timer de malus
        });
        malusTimer.setRepeats(false); // Exécuter une seule fois
        malusTimer.start();  // Démarrer le timer
    }

    private void endGame() {
        String winner = (ScoreP1 >= MAX_SCORE) ? "Player 1 wins!" : "Player 2 wins!";
        JOptionPane.showMessageDialog(this, winner, "Game Over", JOptionPane.INFORMATION_MESSAGE);
        timer.stop();  // Arrêter le jeu
        bonusTimer.stop();  // Arrêter le timer des bonus/malus
        if (malusTimer != null) {
            malusTimer.stop();  // Arrêter le timer de malus
        }
        timer.stop();
        bonusTimer.stop();
        ambianceSound.stop();  // Arrêter le son d'ambiance à la fin du jeu
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_W) {
            paddle1MovingUp = true;
            paddle1MovingDown = false;
            if (!paddle1MoveTimer.isRunning()) {
                paddle1MoveTimer.start();
            }
        }
        if (e.getKeyCode() == KeyEvent.VK_S) {
            paddle1MovingDown = true;
            paddle1MovingUp = false;
            if (!paddle1MoveTimer.isRunning()) {
                paddle1MoveTimer.start();
            }
        }

        if (e.getKeyCode() == KeyEvent.VK_UP) {
            paddle2MovingUp = true;
            paddle2MovingDown = false;
            if (!paddle2MoveTimer.isRunning()) {
                paddle2MoveTimer.start();
            }
        }
        if (e.getKeyCode() == KeyEvent.VK_DOWN) {
            paddle2MovingDown = true;
            paddle2MovingUp = false;
            if (!paddle2MoveTimer.isRunning()) {
                paddle2MoveTimer.start();
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        // Paddle 1 (gauche)
        if (e.getKeyCode() == KeyEvent.VK_W) {
            paddle1MovingUp = false;
        }
        if (e.getKeyCode() == KeyEvent.VK_S) {
            paddle1MovingDown = false;
        }
        if (!paddle1MovingUp && !paddle1MovingDown) {
            paddle1MoveTimer.stop();
        }

        // Paddle 2 (droite)
        if (e.getKeyCode() == KeyEvent.VK_UP) {
            paddle2MovingUp = false;
        }
        if (e.getKeyCode() == KeyEvent.VK_DOWN) {
            paddle2MovingDown = false;
        }
        if (!paddle2MovingUp && !paddle2MovingDown) {
            paddle2MoveTimer.stop();
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    public static void main(String[] args) {
        JFrame frame = new JFrame("Pong Game with Sound");
        PongGame pongGame = new PongGame();
        frame.add(pongGame);
        
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}