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

    private final int MAX_SCORE = 10;  // Limite du score

    // Bonus/Malus
    private int bonusX = -100, bonusY = -100, bonusWidth = 100, bonusHeight = 100;
    private boolean bonusActive = false;
    private boolean isBonus = true;  // Bonus (vert) ou malus (rouge)

    private Timer timer;
    private Timer paddle1MoveTimer;
    private Timer paddle2MoveTimer;
    private Timer bonusTimer;

    private boolean paddle1MovingUp = false, paddle1MovingDown = false;
    private boolean paddle2MovingUp = false, paddle2MovingDown = false;

    private Random random;

    // Variable pour suivre quel paddle a touché la balle en dernier
    private boolean lastHitByPaddle1 = false; 

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
        g.setFont(new Font("Arial", Font.BOLD, 20));  // Définir une police plus grande
        g.drawString("Player 1: " + ScoreP1, 50, 30);  // Afficher le score du joueur 1
        g.drawString("Player 2: " + ScoreP2, getWidth() - 150, 30);  // Afficher le score du joueur 2
        
        // Dessiner le bonus/malus s'il est actif
        if (bonusActive) {
            g.setColor(isBonus ? Color.GREEN : Color.RED);
            g.fillRect(bonusX, bonusY, bonusWidth, bonusHeight);
        }
    }

    public void actionPerformed(ActionEvent e) {
        if (ScoreP1 >= MAX_SCORE || ScoreP2 >= MAX_SCORE) {
            endGame();  // Vérifier si l'un des joueurs a atteint le score maximal
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
            lastHitByPaddle1 = true;
        }
    
        // Paddle 2 touche la balle
        if (ballX >= getWidth() - paddleWidth - ballDiameter && ballY + ballDiameter >= paddle2Y && ballY <= paddle2Y + paddle2Height) {
            ballXSpeed = -ballXSpeed;
            lastHitByPaddle1 = false;
        }
    
        // Gérer les sorties de balle
        if (ballX < 0 || ballX > getWidth()) {
            scoreP();  // Mettre à jour les scores lorsque la balle sort
        }
    
        // Gérer la collision de la balle avec le bonus/malus
        if (bonusActive && ballX + ballDiameter >= bonusX && ballX <= bonusX + bonusWidth &&
            ballY + ballDiameter >= bonusY && ballY <= bonusY + bonusHeight) {
            applyBonusOrMalus();
            bonusActive = false;  // Désactiver le bonus/malus après collision
        }
    
        repaint();
    }

    // Méthode pour déplacer la paddle 1 en fonction de la direction
    private void movePaddle1() {
        if (paddle1MovingUp && paddle1Y > 0) {
            paddle1Y -= 5;
        }
        if (paddle1MovingDown && paddle1Y < getHeight() - paddle1Height) {
            paddle1Y += 5;
        }
    }

    // Méthode pour déplacer la paddle 2 en fonction de la direction
    private void movePaddle2() {
        if (paddle2MovingUp && paddle2Y > 0) {
            paddle2Y -= 5;
        }
        if (paddle2MovingDown && paddle2Y < getHeight() - paddle2Height) {
            paddle2Y += 5;
        }
    }

    public void scoreP() {
        if (ballX < 0) {  // Si la balle sort du côté gauche
            ScoreP2 += 1;  // Le joueur 2 marque un point
        } else if (ballX > getWidth()) {  // Si la balle sort du côté droit
            ScoreP1 += 1;  // Le joueur 1 marque un point
        }
        
        // Réinitialiser la position de la balle
        ballX = getWidth() / 2 - ballDiameter / 2;
        ballY = getHeight() / 2 - ballDiameter / 2;
        
        // Réinitialiser la vitesse de la balle
        ballXSpeed = 5;
        ballYSpeed = 5;
    }

    // Génération aléatoire d'un bonus/malus
    public void spawnBonus() {
        bonusX = random.nextInt(getWidth() - bonusWidth);
        bonusY = random.nextInt(getHeight() - bonusHeight);
        isBonus = random.nextBoolean();  // Choisir si c'est un bonus ou un malus
        bonusActive = true;  // Activer le bonus/malus
    }

    // Appliquer les effets du bonus ou malus
    public void applyBonusOrMalus() {
        if (isBonus) {
            // Appliquer un bonus : augmenter la vitesse de la balle
            ballXSpeed += 2;
            ballYSpeed += 2;
        } else {
            // Appliquer un malus : réduire la taille du paddle qui a touché la balle en dernier
            if (lastHitByPaddle1) {
                paddle1Height = Math.max(60, paddle1Height - 20); // Réduire la taille du paddle 1
            } else {
                paddle2Height = Math.max(60, paddle2Height - 20); // Réduire la taille du paddle 2
            }
        }
    }

    private void endGame() {
        String winner = (ScoreP1 >= MAX_SCORE) ? "Player 1 wins!" : "Player 2 wins!";
        JOptionPane.showMessageDialog(this, winner, "Game Over", JOptionPane.INFORMATION_MESSAGE);
        timer.stop();  // Arrêter le jeu
        bonusTimer.stop();  // Arrêter le timer des bonus/malus
    }

    @Override
    public void keyPressed(KeyEvent e) {
        // Paddle 1 (gauche)
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

        // Paddle 2 (droite)
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
        JFrame frame = new JFrame("Pong Game with Bonus/Malus");
        PongGame game = new PongGame();
        frame.add(game);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}
