import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Random;

public class GamePanel extends JPanel {

    // ==============================
    // SCREEN
    // ==============================

    private static final int SCREEN_WIDTH = 700;
    private static final int SCREEN_HEIGHT = 760;

    // ==============================
    // GAME BOARD
    // ==============================

    private static final int GAME_WIDTH = 600;
    private static final int GAME_HEIGHT = 600;

    private static final int UNIT_SIZE = 20;

    private static final int GAME_X = 50;
    private static final int GAME_Y = 100;

    private static final int MAX_BODY_PARTS =
            (GAME_WIDTH / UNIT_SIZE) *
                    (GAME_HEIGHT / UNIT_SIZE);

    // ==============================
    // SNAKE
    // ==============================

    private final int[] x = new int[MAX_BODY_PARTS];
    private final int[] y = new int[MAX_BODY_PARTS];

    private int bodyParts = 3;

    // ==============================
    // FOOD
    // ==============================

    private int foodX;
    private int foodY;

    /*
        1 = Normal food
        2 = Bonus food
        3 = Special food
    */
    private int foodType = 1;

    // How long special food stays
    private int specialFoodTimer = 0;

    // ==============================
    // SCORE
    // ==============================

    private int score = 0;
    private int highScore = 0;

    // ==============================
    // LEVEL
    // ==============================

    private int level = 1;

    // ==============================
    // DIRECTION
    // ==============================

    private char direction = 'R';

    // ==============================
    // GAME STATE
    // ==============================

    private boolean running = false;
    private boolean paused = false;

    private boolean levelUp = false;
    private int levelUpCounter = 0;

    // ==============================
    // RANDOM
    // ==============================

    private final Random random = new Random();

    // ==============================
    // TIMER
    // ==============================

    private Timer timer;

    // ==============================
    // BUTTONS
    // ==============================

    private JButton restartButton;
    private JButton pauseButton;


    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public GamePanel() {

        setPreferredSize(
                new Dimension(
                        SCREEN_WIDTH,
                        SCREEN_HEIGHT
                )
        );

        setFocusable(true);

        setLayout(null);

        createButtons();

        addKeyListener(new KeyAdapter() {

            @Override
            public void keyPressed(KeyEvent e) {

                // LEFT
                if (e.getKeyCode() == KeyEvent.VK_LEFT) {

                    if (direction != 'R') {
                        direction = 'L';
                    }
                }

                // RIGHT
                if (e.getKeyCode() == KeyEvent.VK_RIGHT) {

                    if (direction != 'L') {
                        direction = 'R';
                    }
                }

                // UP
                if (e.getKeyCode() == KeyEvent.VK_UP) {

                    if (direction != 'D') {
                        direction = 'U';
                    }
                }

                // DOWN
                if (e.getKeyCode() == KeyEvent.VK_DOWN) {

                    if (direction != 'U') {
                        direction = 'D';
                    }
                }

                // SPACE = Pause
                if (e.getKeyCode() == KeyEvent.VK_SPACE) {

                    togglePause();
                }

                // ENTER = Restart
                if (
                        e.getKeyCode() == KeyEvent.VK_ENTER
                                && !running
                ) {

                    startGame();
                }
            }
        });

        startGame();
    }


    // =========================================================
    // BUTTONS
    // =========================================================

    private void createButtons() {

        restartButton = new JButton("Restart");

        restartButton.setBounds(
                240,
                715,
                100,
                30
        );

        restartButton.setFocusPainted(false);

        restartButton.addActionListener(e -> {

            startGame();

            requestFocusInWindow();
        });

        add(restartButton);


        pauseButton = new JButton("Pause");

        pauseButton.setBounds(
                360,
                715,
                100,
                30
        );

        pauseButton.setFocusPainted(false);

        pauseButton.addActionListener(e -> {

            togglePause();

            requestFocusInWindow();
        });

        add(pauseButton);
    }


    // =========================================================
    // START GAME
    // =========================================================

    private void startGame() {

        bodyParts = 3;

        score = 0;

        level = 1;

        direction = 'R';

        running = true;

        paused = false;

        levelUp = false;

        levelUpCounter = 0;

        specialFoodTimer = 0;


        // Starting snake position

        x[0] = GAME_X + 300;

        y[0] = GAME_Y + 300;


        x[1] = x[0] - UNIT_SIZE;

        y[1] = y[0];


        x[2] = x[1] - UNIT_SIZE;

        y[2] = y[1];


        // Create food

        newFood();


        // Stop old timer

        if (timer != null) {

            timer.stop();
        }


        // Starting speed = 200 ms

        timer = new Timer(
                200,
                e -> gameLoop()
        );

        timer.start();


        pauseButton.setText("Pause");

        requestFocusInWindow();

        repaint();
    }


    // =========================================================
    // GAME LOOP
    // =========================================================

    private void gameLoop() {

        if (!running || paused) {

            return;
        }


        move();

        checkFood();

        checkSpecialFoodTimer();

        checkCollisions();

        updateLevelUpMessage();

        repaint();
    }


    // =========================================================
    // MOVE
    // =========================================================

    private void move() {

        // Move body

        for (
                int i = bodyParts - 1;
                i > 0;
                i--
        ) {

            x[i] = x[i - 1];

            y[i] = y[i - 1];
        }


        // Move head

        switch (direction) {

            case 'R':

                x[0] += UNIT_SIZE;

                break;

            case 'L':

                x[0] -= UNIT_SIZE;

                break;

            case 'U':

                y[0] -= UNIT_SIZE;

                break;

            case 'D':

                y[0] += UNIT_SIZE;

                break;
        }
    }


    // =========================================================
    // CHECK FOOD
    // =========================================================

    private void checkFood() {

        if (
                x[0] == foodX &&
                        y[0] == foodY
        ) {

            // NORMAL FOOD
            if (foodType == 1) {

                score += 1;

            }

            // BONUS FOOD
            else if (foodType == 2) {

                score += 3;

            }

            // SPECIAL FOOD
            else if (foodType == 3) {

                score += 5;
            }


            // Snake grows

            bodyParts++;


            // High score

            if (score > highScore) {

                highScore = score;
            }


            // Check level

            checkLevel();


            // Create new food

            newFood();
        }
    }


    // =========================================================
    // CREATE FOOD
    // =========================================================

    private void newFood() {

        boolean validPosition = false;


        while (!validPosition) {

            int columns =
                    GAME_WIDTH / UNIT_SIZE;

            int rows =
                    GAME_HEIGHT / UNIT_SIZE;


            foodX =
                    GAME_X +
                            random.nextInt(columns)
                                    * UNIT_SIZE;


            foodY =
                    GAME_Y +
                            random.nextInt(rows)
                                    * UNIT_SIZE;


            validPosition = true;


            // Don't place food on snake

            for (
                    int i = 0;
                    i < bodyParts;
                    i++
            ) {

                if (
                        foodX == x[i] &&
                                foodY == y[i]
                ) {

                    validPosition = false;

                    break;
                }
            }
        }


        // Decide food type

        int chance = random.nextInt(100);


        if (chance < 70) {

            // 70% normal

            foodType = 1;

            specialFoodTimer = 0;

        }

        else if (chance < 90) {

            // 20% bonus

            foodType = 2;

            specialFoodTimer = 0;

        }

        else {

            // 10% special

            foodType = 3;

            specialFoodTimer = 50;
        }
    }


    // =========================================================
    // SPECIAL FOOD TIMER
    // =========================================================

    private void checkSpecialFoodTimer() {

        if (foodType == 3) {

            specialFoodTimer--;


            if (specialFoodTimer <= 0) {

                newFood();
            }
        }
    }


    // =========================================================
    // LEVEL SYSTEM
    // =========================================================

    private void checkLevel() {

        int newLevel;


        if (score < 5) {

            newLevel = 1;

        }

        else if (score < 10) {

            newLevel = 2;

        }

        else if (score < 15) {

            newLevel = 3;

        }

        else if (score < 20) {

            newLevel = 4;

        }

        else {

            newLevel = 5;
        }


        // Level increased

        if (newLevel > level) {

            level = newLevel;

            levelUp = true;

            levelUpCounter = 15;
        }


        increaseSpeed();
    }


    // =========================================================
    // SPEED
    // =========================================================

    private void increaseSpeed() {

        if (score < 5) {

            timer.setDelay(200);

        }

        else if (score < 10) {

            timer.setDelay(180);

        }

        else if (score < 15) {

            timer.setDelay(160);

        }

        else if (score < 20) {

            timer.setDelay(140);

        }

        else {

            timer.setDelay(120);
        }
    }


    // =========================================================
    // LEVEL UP MESSAGE
    // =========================================================

    private void updateLevelUpMessage() {

        if (levelUpCounter > 0) {

            levelUpCounter--;

        }

        else {

            levelUp = false;
        }
    }


    // =========================================================
    // COLLISION
    // =========================================================

    private void checkCollisions() {

        // Wall collision

        if (
                x[0] < GAME_X ||
                        x[0] >= GAME_X + GAME_WIDTH ||
                        y[0] < GAME_Y ||
                        y[0] >= GAME_Y + GAME_HEIGHT
        ) {

            gameOver();

            return;
        }


        // Self collision

        for (
                int i = bodyParts - 1;
                i > 0;
                i--
        ) {

            if (
                    x[0] == x[i] &&
                            y[0] == y[i]
            ) {

                gameOver();

                return;
            }
        }
    }


    // =========================================================
    // GAME OVER
    // =========================================================

    private void gameOver() {

        running = false;

        paused = false;

        timer.stop();

        pauseButton.setText("Pause");

        repaint();
    }


    // =========================================================
    // PAUSE
    // =========================================================

    private void togglePause() {

        if (!running) {

            return;
        }


        paused = !paused;


        if (paused) {

            pauseButton.setText("Resume");

        }

        else {

            pauseButton.setText("Pause");
        }


        repaint();

        requestFocusInWindow();
    }


    // =========================================================
    // DRAW EVERYTHING
    // =========================================================

    @Override
    protected void paintComponent(Graphics g) {

        super.paintComponent(g);


        Graphics2D g2 =
                (Graphics2D) g;


        g2.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
        );


        drawBackground(g2);

        drawHeader(g2);

        drawGameBoard(g2);

        drawFood(g2);

        drawSnake(g2);


        if (levelUp) {

            drawLevelUp(g2);
        }


        if (paused) {

            drawPauseScreen(g2);
        }


        if (!running) {

            drawGameOverScreen(g2);
        }
    }


    // =========================================================
    // BACKGROUND
    // =========================================================

    private void drawBackground(Graphics2D g2) {

        GradientPaint gradient =
                new GradientPaint(
                        0,
                        0,
                        new Color(15, 23, 42),
                        0,
                        SCREEN_HEIGHT,
                        new Color(2, 6, 23)
                );


        g2.setPaint(gradient);


        g2.fillRect(
                0,
                0,
                SCREEN_WIDTH,
                SCREEN_HEIGHT
        );
    }


    // =========================================================
    // HEADER
    // =========================================================

    private void drawHeader(Graphics2D g2) {

        g2.setColor(Color.WHITE);


        g2.setFont(
                new Font(
                        "Arial",
                        Font.BOLD,
                        30
                )
        );


        g2.drawString(
                "SNAKE",
                50,
                45
        );


        g2.setFont(
                new Font(
                        "Arial",
                        Font.BOLD,
                        16
                )
        );


        g2.setColor(
                new Color(148, 163, 184)
        );


        g2.drawString(
                "Score: " + score,
                350,
                40
        );


        g2.drawString(
                "Best: " + highScore,
                450,
                40
        );


        g2.drawString(
                "Level: " + level,
                560,
                40
        );
    }


    // =========================================================
    // GAME BOARD
    // =========================================================

    private void drawGameBoard(Graphics2D g2) {

        // Board

        g2.setColor(
                new Color(15, 23, 42)
        );


        g2.fillRoundRect(
                GAME_X,
                GAME_Y,
                GAME_WIDTH,
                GAME_HEIGHT,
                20,
                20
        );


        // Grid

        g2.setColor(
                new Color(30, 41, 59)
        );


        for (
                int i = GAME_X;
                i <= GAME_X + GAME_WIDTH;
                i += UNIT_SIZE
        ) {

            g2.drawLine(
                    i,
                    GAME_Y,
                    i,
                    GAME_Y + GAME_HEIGHT
            );
        }


        for (
                int i = GAME_Y;
                i <= GAME_Y + GAME_HEIGHT;
                i += UNIT_SIZE
        ) {

            g2.drawLine(
                    GAME_X,
                    i,
                    GAME_X + GAME_WIDTH,
                    i
            );
        }


        // Border

        g2.setColor(
                new Color(71, 85, 105)
        );


        g2.setStroke(
                new BasicStroke(3)
        );


        g2.drawRoundRect(
                GAME_X,
                GAME_Y,
                GAME_WIDTH,
                GAME_HEIGHT,
                20,
                20
        );
    }


    // =========================================================
    // SNAKE
    // =========================================================

    private void drawSnake(Graphics2D g2) {

        for (
                int i = bodyParts - 1;
                i >= 0;
                i--
        ) {

            if (i == 0) {

                // Head

                g2.setColor(
                        new Color(34, 197, 94)
                );

            }

            else {

                // Body

                g2.setColor(
                        new Color(22, 163, 74)
                );
            }


            g2.fillRoundRect(
                    x[i] + 2,
                    y[i] + 2,
                    UNIT_SIZE - 4,
                    UNIT_SIZE - 4,
                    8,
                    8
            );
        }


        drawSnakeEyes(g2);
    }


    // =========================================================
    // SNAKE EYES
    // =========================================================

    private void drawSnakeEyes(Graphics2D g2) {

        g2.setColor(Color.WHITE);


        int eyeSize = 4;


        if (direction == 'R') {

            g2.fillOval(
                    x[0] + 12,
                    y[0] + 5,
                    eyeSize,
                    eyeSize
            );

            g2.fillOval(
                    x[0] + 12,
                    y[0] + 12,
                    eyeSize,
                    eyeSize
            );

        }

        else if (direction == 'L') {

            g2.fillOval(
                    x[0] + 4,
                    y[0] + 5,
                    eyeSize,
                    eyeSize
            );

            g2.fillOval(
                    x[0] + 4,
                    y[0] + 12,
                    eyeSize,
                    eyeSize
            );

        }

        else if (direction == 'U') {

            g2.fillOval(
                    x[0] + 5,
                    y[0] + 4,
                    eyeSize,
                    eyeSize
            );

            g2.fillOval(
                    x[0] + 12,
                    y[0] + 4,
                    eyeSize,
                    eyeSize
            );

        }

        else {

            g2.fillOval(
                    x[0] + 5,
                    y[0] + 12,
                    eyeSize,
                    eyeSize
            );

            g2.fillOval(
                    x[0] + 12,
                    y[0] + 12,
                    eyeSize,
                    eyeSize
            );
        }
    }


    // =========================================================
    // FOOD
    // =========================================================

    private void drawFood(Graphics2D g2) {

        // NORMAL FOOD
        if (foodType == 1) {

            drawNormalFood(g2);

        }

        // BONUS FOOD
        else if (foodType == 2) {

            drawBonusFood(g2);

        }

        // SPECIAL FOOD
        else {

            drawSpecialFood(g2);
        }
    }


    // =========================================================
    // NORMAL FOOD
    // =========================================================

    private void drawNormalFood(Graphics2D g2) {

        // Shadow

        g2.setColor(
                new Color(127, 29, 29)
        );


        g2.fillOval(
                foodX + 2,
                foodY + 4,
                UNIT_SIZE - 4,
                UNIT_SIZE - 4
        );


        // Apple

        g2.setColor(
                new Color(239, 68, 68)
        );


        g2.fillOval(
                foodX + 2,
                foodY + 2,
                UNIT_SIZE - 4,
                UNIT_SIZE - 4
        );


        // Highlight

        g2.setColor(
                new Color(254, 202, 202)
        );


        g2.fillOval(
                foodX + 6,
                foodY + 5,
                4,
                4
        );
    }


    // =========================================================
    // BONUS FOOD
    // =========================================================

    private void drawBonusFood(Graphics2D g2) {

        g2.setColor(
                new Color(250, 204, 21)
        );


        g2.setFont(
                new Font(
                        "Arial",
                        Font.BOLD,
                        22
                )
        );


        g2.drawString(
                "★",
                foodX,
                foodY + 19
        );
    }


    // =========================================================
    // SPECIAL FOOD
    // =========================================================

    private void drawSpecialFood(Graphics2D g2) {

        g2.setColor(
                new Color(168, 85, 247)
        );


        g2.fillOval(
                foodX + 1,
                foodY + 1,
                UNIT_SIZE - 2,
                UNIT_SIZE - 2
        );


        g2.setColor(Color.WHITE);


        g2.setFont(
                new Font(
                        "Arial",
                        Font.BOLD,
                        14
                )
        );


        g2.drawString(
                "5",
                foodX + 6,
                foodY + 15
        );
    }


    // =========================================================
    // LEVEL UP
    // =========================================================

    private void drawLevelUp(Graphics2D g2) {

        g2.setColor(
                new Color(34, 197, 94, 220)
        );


        g2.fillRoundRect(
                190,
                280,
                320,
                90,
                20,
                20
        );


        g2.setColor(Color.WHITE);


        g2.setFont(
                new Font(
                        "Arial",
                        Font.BOLD,
                        32
                )
        );


        g2.drawString(
                "LEVEL UP!",
                265,
                320
        );


        g2.setFont(
                new Font(
                        "Arial",
                        Font.PLAIN,
                        18
                )
        );


        g2.drawString(
                "Level " + level,
                315,
                350
        );
    }


    // =========================================================
    // PAUSE SCREEN
    // =========================================================

    private void drawPauseScreen(Graphics2D g2) {

        drawOverlay(g2);


        g2.setColor(Color.WHITE);


        g2.setFont(
                new Font(
                        "Arial",
                        Font.BOLD,
                        42
                )
        );


        g2.drawString(
                "PAUSED",
                260,
                350
        );


        g2.setFont(
                new Font(
                        "Arial",
                        Font.PLAIN,
                        18
                )
        );


        g2.drawString(
                "Press SPACE to resume",
                235,
                385
        );
    }


    // =========================================================
    // GAME OVER
    // =========================================================

    private void drawGameOverScreen(Graphics2D g2) {

        drawOverlay(g2);


        g2.setColor(
                new Color(248, 113, 113)
        );


        g2.setFont(
                new Font(
                        "Arial",
                        Font.BOLD,
                        42
                )
        );


        g2.drawString(
                "GAME OVER",
                225,
                320
        );


        g2.setColor(Color.WHITE);


        g2.setFont(
                new Font(
                        "Arial",
                        Font.BOLD,
                        22
                )
        );


        g2.drawString(
                "Score: " + score,
                285,
                360
        );


        g2.setFont(
                new Font(
                        "Arial",
                        Font.PLAIN,
                        17
                )
        );


        g2.drawString(
                "Press ENTER or Restart to play again",
                195,
                400
        );
    }


    // =========================================================
    // OVERLAY
    // =========================================================

    private void drawOverlay(Graphics2D g2) {

        g2.setColor(
                new Color(2, 6, 23, 190)
        );


        g2.fillRoundRect(
                GAME_X,
                GAME_Y,
                GAME_WIDTH,
                GAME_HEIGHT,
                20,
                20
        );
    }
}