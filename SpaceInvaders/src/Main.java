import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Random;

class SpaceInvaders extends JPanel{
    int tileSize = 32;
    int rows = 16;
    int columns = 16;
    int boardWidth = tileSize*columns;
    int boardHeight = tileSize*rows;
    int shipVelocity = tileSize;
    int score = 0;
    boolean gameOver = false;

    JLabel scoreLabel;
    JButton restartButton;

    Image al;
    Image alCy;
    Image alMag;
    Image alYl;
    Image ship;
    ArrayList<Image> alienImages;

    Block spaceShip;
    ArrayList<Block> alienList;

    ArrayList<Block> bullets;
    int bulletVelocityY = -10;

    int alienRows = 2;
    int alienColumns = 3;
    int alienCount = 0;
    int alienVelocityX = 2;

    Timer gameLoop;

    class Block{
        int x;
        int y;
        int width;
        int height;
        Image img;
        boolean alive = true;
        boolean used = false;

        Block(int x, int y, int width, int height, Image img){
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.img = img;
        }
    }

    SpaceInvaders(JLabel scoreLabel, JButton restartButton){
        setPreferredSize(new Dimension(boardWidth,boardHeight));
        setBackground(Color.black);
        setFocusable(true);

        this.scoreLabel = scoreLabel;
        this.restartButton = restartButton;

        al = new ImageIcon(getClass().getResource("/alien.png")).getImage();
        alCy = new ImageIcon(getClass().getResource("/alien-cyan.png")).getImage();
        alMag = new ImageIcon(getClass().getResource("/alien-magenta.png")).getImage();
        alYl = new ImageIcon(getClass().getResource("/alien-yellow.png")).getImage();
        ship = new ImageIcon(getClass().getResource("/ship.png")).getImage();

        alienList = new ArrayList<Block>();
        alienImages = new ArrayList<Image>();
        bullets = new ArrayList<Block>();
        alienImages.add(al);
        alienImages.add(alCy);
        alienImages.add(alMag);
        alienImages.add(alYl);

        spaceShip = new Block(tileSize*columns/2 - tileSize, boardHeight - tileSize*2, 2*tileSize, tileSize, ship);

        gameLoop = new Timer(1000 / 40, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                move();
                repaint();
                if(!gameOver){
                    scoreLabel.setText("Score: " + score);
                }
                else{
                    scoreLabel.setText("Game Over: " + score);
                    gameLoop.stop();
                }
            }
        });
        createAliens();
        gameLoop.start();

        addKeyListener(new KeyListener() {
            @Override
            public void keyPressed(KeyEvent e) {
                if(e.getKeyCode() == KeyEvent.VK_RIGHT && spaceShip.x < boardWidth - 2*tileSize){
                    spaceShip.x += shipVelocity;
                }
                else if(e.getKeyCode() == KeyEvent.VK_LEFT && spaceShip.x > 0){
                    spaceShip.x -= shipVelocity;
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                if(e.getKeyCode() == KeyEvent.VK_SPACE){
                    Block bullet = new Block(spaceShip.x + spaceShip.width*15/32, spaceShip.y, tileSize/8, tileSize/2, null);
                    bullets.add(bullet);
                }
            }
            @Override
            public void keyTyped(KeyEvent e) {}
        });

        restartButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                gameOver = false;
                alienVelocityX = 2;
                alienList.clear();
                bullets.clear();
                score = 0;
                alienColumns = 3;
                alienRows = 2;
                scoreLabel.setText("Score: " + score);
                createAliens();
                gameLoop.start();
            }
        });
    }

    public void paintComponent(Graphics g){
        super.paintComponent(g);
        draw(g);
    }

    public void draw(Graphics g){
        for(int i=0;i<columns;i++){
            g.drawLine(i*tileSize, 0, i*tileSize, boardHeight);
        }
        for(int i=0;i<rows;i++){
            g.drawLine(0,i*tileSize,boardWidth, i*tileSize);
        }

        g.drawImage(spaceShip.img, spaceShip.x, spaceShip.y, spaceShip.width, spaceShip.height, null);

        for(Block block: alienList){
            if(block.alive){
                g.drawImage(block.img, block.x, block.y, block.width, block.height, null);
            }
        }

        g.setColor(Color.red);
        for(Block block: bullets){
            if(!block.used){
                g.fillRect(block.x, block.y, block.width, block.height);
            }
        }
    }

    public void move(){
        for(Block block: alienList){
            if(block.alive){
                block.x += alienVelocityX;

                if(block.x + block.width >= boardWidth || block.x <= 0){
                    alienVelocityX *= -1;
                    block.x += alienVelocityX*2;

                    for(Block alien: alienList){
                        alien.y += alien.height;
                    }
                }

                if(block.y >= spaceShip.y){
                    gameOver = true;
                }
            }
        }

        for(Block bullet: bullets){
            bullet.y += bulletVelocityY;

            for(Block alien: alienList){
                if(!bullet.used && alien.alive && detectCollision(bullet, alien)){
                    bullet.used = true;
                    alien.alive = false;
                    alienCount--;
                    score += 15;
                }
            }
        }

        while(!bullets.isEmpty() && bullets.get(0).y < 0){
            bullets.remove(0);
        }

        if(alienCount == 0){
            alienColumns = Math.min(alienColumns + 1, 6);
            alienRows = Math.min(alienRows + 1, 9);
            bullets.clear();
            alienList.clear();
            createAliens();
        }
    }

    public void createAliens(){
        Random random = new Random();
        for(int i=0;i<alienColumns;i++){
            for(int j=0;j<alienRows;j++){
                int randomImg = random.nextInt(alienImages.size());
                Block alien = new Block(tileSize + i*tileSize*2,tileSize + j*tileSize,tileSize*2, tileSize, alienImages.get(randomImg));
                alienList.add(alien);
            }
        }
        alienCount = alienList.size();
    }

    public boolean detectCollision(Block a, Block b) {
        return  a.x < b.x + b.width &&
                a.x + a.width > b.x &&
                a.y < b.y + b.height &&
                a.y + a.height > b.y;
    }
}

public class Main {
    public static void main(String[] args) {
        int tileSize = 32;
        int rows = 16;
        int columns = 16;
        int boardWidth = tileSize*columns;
        int boardHeight = tileSize*rows + 50;

        JFrame frame = new JFrame("Space Invaders");
        frame.setSize(boardWidth,boardHeight);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.setIconImage(new ImageIcon(Main.class.getResource("/alien-magenta.png")).getImage());
        frame.setLayout(new BorderLayout());

        JPanel scorePanel = new JPanel();
        scorePanel.setLayout(new BorderLayout());
        scorePanel.setBackground(Color.black);

        JLabel scoreLabel = new JLabel();
        scoreLabel.setFont(new Font("Arial",Font.BOLD,20));
        scoreLabel.setForeground(Color.white);
        scoreLabel.setText("Score: 0");
        scorePanel.add(scoreLabel, BorderLayout.WEST);

        JButton restartButton = new JButton();
        restartButton.setFont(new Font("Arial",Font.BOLD,18));
        restartButton.setForeground(Color.ORANGE);
        restartButton.setBackground(Color.black);
        restartButton.setText("Restart");
        restartButton.setFocusable(false);
        scorePanel.add(restartButton, BorderLayout.EAST);

        frame.add(scorePanel, BorderLayout.NORTH);

        SpaceInvaders s = new SpaceInvaders(scoreLabel, restartButton);
        frame.add(s);
        frame.pack();
        s.requestFocus();
        frame.setVisible(true);
    }
}