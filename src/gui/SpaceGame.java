package gui;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.CacheHint;
import javafx.scene.Node;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;
import javafx.util.Pair;
import main.Layouts;
import main.Root;
import main.Size;
import main.UtilAndConstants;
import searchengine.StemIndex;

import javax.swing.text.Style;
import java.security.Key;
import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.Random;
import java.util.function.Consumer;

public class SpaceGame extends StackPane {

    private Main wrapper;
    private Ship ship;
    private Pane frontPlane;
    private Pane backPlane;
    private double width;
    private double height;
    private EventHandler<KeyEvent> pressHandler;
    private EventHandler<KeyEvent> releaseHandler;
    private int score = 0;
    private boolean scoreLocked = false;
    private int highScore;

    public static final double ADVANCE_SPEED = 400;
    private EventHandler<KeyEvent> gameOverHandler;
    private int gameOverState = -1;
    private HashSet<Timeline> allTimelines;

    public SpaceGame(Main wrapper) {
        this.wrapper = wrapper;
        width = UtilAndConstants.DEFAULT_WIDTH;
        height = 130;
        allTimelines = new HashSet<>();
        setPrefHeight(Size.height(height));
        setMinHeight(Size.height(height));
        getChildren().add(new Layouts.Filler()); //just a hack to make it fill the bar
        Styles.setBackgroundColor(this, Color.BLACK);
        ship = new Ship(true, CollisionType.ALLY);
        frontPlane = new Pane();
        backPlane = new Pane();
    }

    void playTimeline(Timeline t) {
        t.play();
        allTimelines.add(t);
    }

    public void setHighScore(int highScore) {
        this.highScore = highScore;
    }

    interface Sprite {
        default void spawn(boolean front) {
            spawn(front, 0, null);
        }

        void spawn(boolean front, int delayMillis, Consumer<Pane> onDespawn);

        void despawn();

        HitBox getHitBox();

        default void explode(SpaceGame game, double radius) {
            game.new Explosion(centerX(), centerY(), radius).spawn(true);
        }

        double centerX();

        double centerY();

        CollisionType getCollision();


        void handleCollision(Sprite o);
    }

    private void runBackground() {
        //put some in the field of play right away
        for (int i = 0; i < 30; i++) {
            DotParticle star = new DotParticle(Size.width(Math.random() * 2000), Math.random() * (height - 10), -1 * Size.width(200), 0);
            star.setColor(Color.gray(.5));
            star.spawn(false);
        }
        //spawn the rest to the right of the screen
        Timeline spawnStars = new Timeline(new KeyFrame(Duration.millis(200), event -> {
            DotParticle star = new DotParticle(Size.width(1920), Math.random() * (height - 10), -1 * Size.width(200), 0);
            star.setColor(Color.gray(.5));
            star.spawn(false);
        }));
        spawnStars.setCycleCount(Animation.INDEFINITE);
        playTimeline(spawnStars);
    }

    private void runIntro() {
        HashSet<LineParticle> activeHyperspaceLines = new HashSet<>();
        for (int i = 0; i < 200; i++) {
            LineParticle line = new LineParticle(Size.width(100), Size.width(1920), Math.random() * (height - 10), -1 * Size.width(4000), 0);
            line.spawn(false, Math.max(0, i - 10) * 15, pane -> {
                activeHyperspaceLines.remove(line);
                backPlane.getChildren().remove(line);
            });
            activeHyperspaceLines.add(line);
        }
        //Come out of hyperspace animation
        Timeline stopDelay = new Timeline(new KeyFrame(Duration.millis(2800)));
        stopDelay.setOnFinished(event -> {
            activeHyperspaceLines.forEach(lineParticle -> {
                lineParticle.xSpeed /= 20.0;
                Timeline newAnimation = new Timeline(new KeyFrame(Duration.millis(500), new KeyValue(lineParticle.strokeProperty(), Color.gray(0.5)), new KeyValue(lineParticle.endXProperty(), lineParticle.startXProperty().doubleValue())));
                newAnimation.setOnFinished(lineParticle.currentAnimation.getOnFinished());
                lineParticle.currentAnimation = newAnimation;
                playTimeline(newAnimation);
            });
            ship.setVisible(true);
            final HashSet<Pair<Sprite, Sprite>> collisionPairs = new HashSet<>();
            Timeline collisionHandler = new Timeline(new KeyFrame(Duration.millis(16), event1 -> {
                int counter = -1;
                for (Node n : frontPlane.getChildren()) {
                    counter++;
                    if (n instanceof Sprite) {
                        HitBox h = ((Sprite) n).getHitBox();
                        if (h == null)
                            continue;
                        for (int i = 0; i < frontPlane.getChildren().size(); i++) {
                            Node o = frontPlane.getChildren().get(i);
                            if (o instanceof Sprite && o != n) {
                                HitBox oh = ((Sprite) o).getHitBox();
                                if (oh == null)
                                    continue;
                                if (h.overlaps(oh)) {
                                    collisionPairs.add(new Pair<>(((Sprite) n), ((Sprite) o)));
                                }
                            }
                        }
                    }
                }
                for (Pair<Sprite, Sprite> pair : collisionPairs) {
                    pair.getKey().handleCollision(pair.getValue());
                }
                collisionPairs.clear();
            }));
            collisionHandler.setCycleCount(Animation.INDEFINITE);
            playTimeline(collisionHandler);
            Timeline shipMoveIn = new Timeline(new KeyFrame(Duration.seconds(1), new KeyValue(ship.translateXProperty(), 200)));
            shipMoveIn.setOnFinished(event1 -> {
                ship.hitBox.translateX(300);
                //enable movement
                ship.unlock();
                new HUD().spawn(true);
                startSpawnAI();
            });
            playTimeline(shipMoveIn);
        });
        playTimeline(stopDelay);
    }

    private void startSpawnAI() {
        Timeline spawnTimer = new Timeline(new KeyFrame(Duration.millis(UtilAndConstants.DEFAULT_WIDTH / ADVANCE_SPEED * 1000), event -> {
            //spawn a new enemy pattern every screen
            Random rand = new Random();
            score(100);
            int loops4 = 4;
            switch (rand.nextInt(10)) {
                case 9:
                case 0: {
                    new LargePlanet(false).spawn(true, 0, null);
                    new SmallPlanet(2).spawn(true, 0, null);
                    new SmallPlanet(1).spawn(true, timeDelay(30), null);
                    new SmallPlanet(2).spawn(true, timeDelay(50), null);
                    new SmallPlanet(2).spawn(true, timeDelay(60), null);
                    new LargePlanet(false).spawn(true, timeDelay(80), null);
                    break;
                }
                case 1: {
                    new LargePlanet(true).spawn(true, 0, null);
                    new SmallPlanet(0).spawn(true, 0, null);
                        new SmallPlanet(0).spawn(true, timeDelay(40), null);
                    new SmallPlanet(0).spawn(true, timeDelay(50), null);
                    if (Math.random() < .5)
                        new PowerUp(2).spawn(true, timeDelay(55), null);
                    new SmallPlanet(0).spawn(true, timeDelay(60), null);
                    new LargePlanet(true).spawn(true, timeDelay(80), null);
                    break;
                }
                case 8:
                case 2: {
                    new SmallPlanet(0).spawn(true, 15, null);
                    new SmallPlanet(2).spawn(true, timeDelay(25), null);
                    new SmallPlanet(1).spawn(true, timeDelay(50), null);
                    new SmallPlanet(0).spawn(true, timeDelay(80), null);
                    new Ship(false, CollisionType.ENEMY).spawn(true, timeDelay(0), null);
                    new Ship(false, CollisionType.ENEMY).spawn(true, timeDelay(5), null);
                    new Ship(false, CollisionType.ENEMY).spawn(true, timeDelay(10), null);
                    break;
                }
                case 3: {
                    if (Math.random() < .5)
                        new PowerUp(rand.nextInt(3)).spawn(true, 0, null);
                    new Ship(false, CollisionType.ENEMY).spawn(true, timeDelay(40), null);
                    new Ship(false, CollisionType.ENEMY).spawn(true, timeDelay(50), null);
                    new Ship(false, CollisionType.ENEMY).spawn(true, timeDelay(60), null);
                    new Ship(false, CollisionType.ENEMY).spawn(true, timeDelay(70), null);
                    new Ship(false, CollisionType.ENEMY).spawn(true, timeDelay(80), null);
                    new Ship(false, CollisionType.ENEMY).spawn(true, timeDelay(90), null);
                    break;
                }
                case 4: {
                    loops4++;
                    if (Math.random() < .5) {
                        new PowerUp(0).spawn(true, 0, null);
                        new PowerUp(2).spawn(true, timeDelay(5), null);
                    }
                    for (int i = 20; i < 80; i+= Math.max(10, 60 / loops4)) {
                        new SmallPlanet(rand.nextInt(3)).spawn(true, timeDelay(i), null);
                    }
                    for (int i = 0; i < 2; i++) {
                        new Ship(false, CollisionType.ENEMY).spawn(true, timeDelay(rand.nextInt(100)), null);
                    }
                    break;
                }
                case 5: {
                    new LargePlanet(true).spawn(true, 0, null);
                    new LargePlanet(false).spawn(true, timeDelay(33.3), null);
                    new LargePlanet(true).spawn(true, timeDelay(66.7), null);
                    break;
                }
                case 6: {
                    new PowerUp(0).spawn(true, 0, null);
                    new PowerUp(2).spawn(true, timeDelay(5), null);
                    for (int i = 0; i < 100; i += 10) {
                        new Ship(false, CollisionType.ENEMY).spawn(true, timeDelay(i), null);
                    }
                    break;
                }
                case 7: {
                    new LargePlanet(true).spawn(true, 0, null);
                    new LargePlanet(false).spawn(true, timeDelay(33.3), null);
                    new LargePlanet(true).spawn(true, timeDelay(66.7), null);
                    for (int i = 0; i < 100; i += 10) {
                        new Ship(false, CollisionType.ENEMY).spawn(true, timeDelay(i), null);
                    }
                    break;

                }
            }
        }));
        spawnTimer.setCycleCount(Animation.INDEFINITE);
        playTimeline(spawnTimer);
    }

    private int timeDelay(double percent) {
        return (int) (UtilAndConstants.DEFAULT_WIDTH / ADVANCE_SPEED * percent * 10);
    }

    private void initializeKeyControls() {
        pressHandler = event -> {
            switch (event.getCode()) {
                case W:
                case UP:
                    ship.up();
                    break;
                case S:
                case DOWN:
                    ship.down();
                    break;
                case SPACE:
                    ship.fire();
                    break;
            }
        };
        releaseHandler = event -> {
            switch (event.getCode()) {
                case W:
                case UP:
                    ship.endUp();
                    break;
                case S:
                case DOWN:
                    ship.endDown();
                    break;
                case SPACE:
                    ship.unfire();
                    break;
                case ESCAPE:
                    wrapper.closeGame();
                    gameOverState = -1;
                    break;
            }
        };
        wrapper.getPrimaryStage().addEventHandler(KeyEvent.KEY_PRESSED, pressHandler);
        wrapper.getPrimaryStage().addEventHandler(KeyEvent.KEY_RELEASED, releaseHandler);
    }

    void start() {
        wrapper.getKeyMap().lock();
        gameOverState = -1;
        getChildren().add(backPlane);
        getChildren().add(frontPlane);
        initializeKeyControls();
        runBackground();
        runIntro();
        ship.setTranslateX(Size.width(-100));
        ship.setTranslateY(Size.height(65));
        ship.hitBox.translateX(Size.height(-100));
        ship.hitBox.translateY(Size.height(65));
        ship.lock();
        ship.spawn(true);
    }

    void quit() {
        //important for stopping gameOver() being called in the background after the game exits
        if (!ship.locked)
            ship.lock();
        wrapper.getKeyMap().unlock();
        wrapper.getPrimaryStage().removeEventHandler(KeyEvent.KEY_PRESSED, pressHandler);
        wrapper.getPrimaryStage().removeEventHandler(KeyEvent.KEY_RELEASED, releaseHandler);
        if (gameOverHandler != null)
            wrapper.getPrimaryStage().removeEventHandler(KeyEvent.KEY_RELEASED, gameOverHandler);
        //stop animations
        for (Timeline t : allTimelines) {
            t.stop();
        }
    }

    private class Ship extends Pane implements Sprite {

        static final int FIRE_RATE = 333;
        static final int RAPID_FIRE_RATE = 67;

        int tripleShot = 0;
        int rapidFire = 0;
        int shield = 0;

        boolean up;
        boolean down;
        boolean firing;
        long lastFire = FIRE_RATE + 1;
        boolean locked;
        private CircleHitBox hitBox;
        private CollisionType collision;
        boolean enemyStopped = true;
        private boolean right;

        public Ship(boolean right, CollisionType type) {
            this.right = right;
            this.collision = type;
            double leftArmOffset = Size.width(right ? 0 : 8);

            Line topArm = new Line(leftArmOffset, 0, Size.width(10 + leftArmOffset), 0);
            Line centerBody = new Line(Size.width(right ? 4 : 0), Size.height(7.5), Size.width(right ? 20 : 13), Size.height(7.5));
            Line bottomArm = new Line(leftArmOffset, Size.height(15), Size.width(10 + leftArmOffset), Size.height(15));
            Line vertical = new Line(Size.width(right ? 5 : 15), 0, Size.width(right ? 5 : 15), Size.height(15));

            topArm.setStroke(Color.WHITE);
            centerBody.setStroke(Color.WHITE);
            bottomArm.setStroke(Color.WHITE);
            vertical.setStroke(Color.WHITE);

            topArm.setStrokeWidth(Size.height(2));
            centerBody.setStrokeWidth(Size.height(3));
            bottomArm.setStrokeWidth(Size.height(2));
            vertical.setStrokeWidth(Size.width(2));

            Circle shieldGraphic = new Circle(Size.width(9), Size.height(7.5), Size.lessWidthHeight(18));
            shieldGraphic.setStroke(Color.WHITE);
            shieldGraphic.setStrokeWidth(LINE_HEIGHT);
            shieldGraphic.setVisible(false);

            getChildren().addAll(shieldGraphic, topArm, centerBody, bottomArm, vertical);

            hitBox = new CircleHitBox(Size.width(7.5), Size.height(7.5), (Size.width(5) + Size.height(5)) / 2.0);

            setCache(true);
            setCacheHint(CacheHint.SPEED);
            setCacheShape(true);

            //responds with 60 fps
            Timeline player = new Timeline(new KeyFrame(Duration.millis(16), event -> {
                if (!locked) {
                    double orig = this.getTranslateY();
                    if (up && !down) {
                        this.setTranslateY(getTranslateY() - Size.height(5));
                    } else if (down && !up) {
                        this.setTranslateY(getTranslateY() + Size.height(5));
                    }
                    if (this.getTranslateY() < 20)
                        this.setTranslateY(20);
                    if (this.getTranslateY() > 105)
                        this.setTranslateY(105);
                    double diff = this.getTranslateY() - orig;
                    this.hitBox.translateY(diff);
                    if (firing && ((lastFire >= FIRE_RATE) || (rapidFire > 0 && (lastFire >= RAPID_FIRE_RATE)))) {
                        lastFire = 0;
                        double xSpeed = Size.width((right ? 1 : -1) * 1000);
                        double offset = Size.width(right ? 20 : -20);
                        LineParticle straight = new LineParticle(Size.width(20), getTranslateX() + offset, Size.height(getTranslateY() + 7.5), xSpeed, 0);
                        straight.setCollision(getCollision());
                        straight.spawn(true);
                        if (tripleShot > 0) {
                            LineParticle up = new LineParticle(Size.width(20), getTranslateX() + offset, Size.height(getTranslateY() + 7.5), xSpeed, Size.height(50));
                            up.setCollision(getCollision());
                            up.spawn(true);
                            LineParticle down = new LineParticle(Size.width(20), getTranslateX() + offset, Size.height(getTranslateY() + 7.5), xSpeed, Size.height(-50));
                            down.setCollision(getCollision());
                            down.spawn(true);
                        }
                    }
                }
                lastFire += 16;
                rapidFire = Math.max(0, rapidFire - 16);
                tripleShot = Math.max(0, tripleShot - 16);
                shield = Math.max(0, shield - 16);
                if (shield > 0)
                    shieldGraphic.setVisible(true);
                else shieldGraphic.setVisible(false);
            }));
            player.setCycleCount(Animation.INDEFINITE);
            playTimeline(player);
            if (!right) {
                Ship playerShip = SpaceGame.this.ship;
                //naive enemy AI that only tries to aim at / avoid the player and no other obstacles
                Timeline enemyAI = new Timeline(new KeyFrame(Duration.millis(250), event -> {
                    //we can only move every other cycle
                    if (enemyStopped) {
                        if (!playerShip.firing) {
                            //move toward the player no matter where they are
                            if (this.getTranslateY() > playerShip.getTranslateY() && Math.random() < .7) {
                                up();
                            } else {
                                down();
                            }
                        } else {
                            //move away from player, but also away from the edge of the screen
                            if ((((this.getTranslateY() < playerShip.getTranslateY()) && (getTranslateY() > Size.height(20))) || (getTranslateY() > Size.height(105))) && Math.random() < .7) {
                                up();
                            } else {
                                down();
                            }
                        }
                        enemyStopped = false;
                    } else {
                        endUp();
                        endDown();
                        enemyStopped = true;
                    }
                    //randomly fire with small probability
                    if (Math.random() < 0.15) {
                        fire();
                    } else {
                        unfire();
                    }
                }));
                enemyAI.setCycleCount(Animation.INDEFINITE);
                playTimeline(enemyAI);
            }
        }

        void unlock() {
            locked = false;
        }

        void lock() {
            locked = true;
        }

        public void fire() {
            firing = true;
        }

        public void unfire() {
            firing = false;
            lastFire = FIRE_RATE + 1;
        }

        void down() {
            down = true;
        }

        void endDown() {
            down = false;
        }

        void up() {
            up = true;
        }

        void endUp() {
            up = false;
        }

        @Override
        public void spawn(boolean front, int delayMillis, Consumer<Pane> onDespawn) {
            if (!right) {
                setTranslateX(Size.width(1950));
                hitBox.translateX(Size.width(1950));
                double dy = Math.random() * 110;
                this.setTranslateY(dy);
                hitBox.translateY(dy);
                Timeline flyLeft = new Timeline(new KeyFrame(Duration.millis(16), event -> {
                     double dx = -0.024 * ADVANCE_SPEED;
                    this.setTranslateX(getTranslateX() + dx);
                    hitBox.translateX(dx);
                    if (getTranslateX() < Size.width(-20))
                        despawn();

                }));
                flyLeft.setCycleCount(Animation.INDEFINITE);
                flyLeft.setDelay(Duration.millis(delayMillis));
                playTimeline(flyLeft);
            }
            frontPlane.getChildren().add(this);
            this.setViewOrder(Integer.MIN_VALUE);
        }

        @Override
        public void despawn() {
            frontPlane.getChildren().remove(this);
            if (getTranslateX() > 0)
                explode(SpaceGame.this, 25);
            if (this == ship && !ship.locked) {
                lock();
                gameOver();
            } else {
                lock();
            }
        }

        @Override
        public HitBox getHitBox() {
            return hitBox;
        }

        @Override
        public double centerX() {
            return getTranslateX() + Size.width(15);
        }

        @Override
        public double centerY() {
            return this.getTranslateY() + Size.height(7.5);
        }

        @Override
        public CollisionType getCollision() {
            return collision;
        }

        @Override
        public void handleCollision(Sprite o) {
            if (o.getCollision() == CollisionType.POWER) {
                if (this == ship) {
                    int type = ((PowerUp) o).type;
                    switch (type) {
                        case 0:
                            ship.rapidFire = 10000;
                            break;
                        case 1:
                            ship.shield = 10000;
                            break;
                        case 2:
                            ship.tripleShot = 10000;
                    }
                }
                return;
            }
            if (o.getCollision() != CollisionType.NONE && o.getCollision() != getCollision()) {
                if (this != ship) {
                    score(100);
                }
                if (shield == 0 || o.getCollision() == CollisionType.ALL)
                    despawn();
            }
        }
    }

    private void gameOver() {
        scoreLocked = true;
        Timeline delay = new Timeline(new KeyFrame(Duration.millis(1500)));
        delay.setOnFinished(event -> {
            if (score > highScore)
                highScore = score;
            VBox gameOver = new VBox();
            Styles.setBackgroundColor(gameOver, Color.BLACK);
            DecimalFormat decimalFormat = new DecimalFormat("####000000");
            String[] messages = {
                    "Game Over",
                    "Score: " + decimalFormat.format(score),
                    score == highScore ? "New High Score!" : "High Score: " + decimalFormat.format(highScore),
                    "This game is intended as a secret, please help keep it that way.",
                    "Press Space to play again, or Escape to quit."

            };

            for (int i = 0; i < messages.length - 2; i++) {
                String s = messages[i];
                Text t = new Text(s);
                t.setFont(CustomFonts.sh_pinscher(i == 0 ? 24 : 20));
                t.setTextAlignment(TextAlignment.CENTER);
                t.setFill(Color.WHITE);
                gameOver.getChildren().add(t);
            }
            gameOver.setAlignment(Pos.CENTER);
            gameOver.setPadding(Size.insets(10));
            gameOver.setViewOrder(Integer.MIN_VALUE);
            SpaceGame.this.getChildren().add(new HBox(new Layouts.Filler(), gameOver, new Layouts.Filler()));
            gameOverState = 0;
            gameOverHandler = event1 -> {
                if (event1.getCode().equals(KeyCode.SPACE)) {
                    if (gameOverState == 0) {
                        gameOver.getChildren().clear();
                        for (int i = messages.length - 2; i < messages.length; i++) {
                            String s = messages[i];
                            Text t = new Text(s);
                            t.setFont(CustomFonts.sh_pinscher(20));
                            t.setTextAlignment(TextAlignment.CENTER);
                            t.setFill(Color.WHITE);
                            gameOver.getChildren().add(t);
                        }
                        gameOverState = 1;
                    } else if (gameOverState == 1) {
                        wrapper.closeGame();
                        wrapper.startGame(false);
                        gameOverState = -1;
                    }
                }
            };
            wrapper.getPrimaryStage().addEventHandler(KeyEvent.KEY_RELEASED, gameOverHandler);
        });
        playTimeline(delay);
    }

    private synchronized void score(int i) {
        if (!scoreLocked) {
            score += i;
        }
    }

    enum CollisionType {
        NONE, ALLY, ENEMY, ALL, POWER;
    }

    private final double LINE_HEIGHT = Size.height(2);

    private class LineParticle extends Line implements Sprite {

        private double length;
        private double x0;
        private double y0;
        //speeds in pixels per second
        private double xSpeed;
        private double ySpeed;
        private boolean front;
        private Consumer<Pane> onDespawn;
        private Timeline currentAnimation;
        private CollisionType collision;
        private RectangleHitBox hitBox;

        public LineParticle(double length, double x0, double y0, double xSpeed, double ySpeed) {
            super();
            this.length = length;
            this.x0 = x0;
            this.y0 = y0;
            this.xSpeed = xSpeed;
            this.ySpeed = ySpeed;
            setStroke(Color.WHITE);
            setStrokeWidth(LINE_HEIGHT);
            setStartX(x0);
            setStartY(y0);
            setEndX(x0 + length);
            setEndY(y0);
            hitBox = new RectangleHitBox(x0, y0, length, LINE_HEIGHT);
            setCache(true);
            setCacheHint(CacheHint.SPEED);
            setCacheShape(true);
        }

        public void setColor(Color color) {
            setStroke(color);
        }

        public void spawn(boolean front) {
            spawn(front, 0, null);
        }

        public void spawn(boolean front, int delayMillis, Consumer<Pane> onDespawn) {
            this.front = front;
            this.onDespawn = onDespawn;
            Pane effectivePane = front ? frontPlane : backPlane;
            effectivePane.getChildren().add(this);

            double xPadding = Size.width(50);
            double yPadding = 0;

            //Timeline animation = new Timeline(new KeyFrame(xSpeed == 0 ? Duration.INDEFINITE : Duration.seconds(xDistance / xSpeed), new KeyValue(this.translateXProperty(), xDistance)),
            //        new KeyFrame(ySpeed == 0 ? Duration.INDEFINITE : Duration.seconds(yDistance / ySpeed), new KeyValue(this.translateYProperty(), yDistance)));
            Timeline animation = new Timeline(new KeyFrame(Duration.millis(8), event -> {
                double dx = Size.width(xSpeed * (0.008));
                this.setTranslateX(getTranslateX() + dx);
                double dy = Size.height(ySpeed * (0.008));
                this.setTranslateY(getTranslateY() + dy);
                hitBox.translateX(dx);
                hitBox.translateY(dy);
                //bounds check
                if (x0 + this.getTranslateX() + xPadding < 0 - length || x0 + this.getTranslateX() - xPadding > Size.width(UtilAndConstants.DEFAULT_WIDTH) || y0 + this.getTranslateY() + yPadding < 0 || y0 + this.getTranslateY() - yPadding > Size.height(125))
                    despawn();
            }));
            animation.setDelay(Duration.millis(delayMillis));
            animation.setCycleCount(Animation.INDEFINITE);
            currentAnimation = animation;
            playTimeline(animation);
        }

        private double getX() {
            return getStartX() + getTranslateX();
        }

        private double getY() {
            return getStartY() + getTranslateY();
        }

        public void despawn() {
            Pane effectivePane = front ? frontPlane : backPlane;
            if (onDespawn == null) {
                effectivePane.getChildren().remove(this);
            } else {
                effectivePane.getChildren().remove(this);
                onDespawn.accept(effectivePane);
            }
        }

        @Override
        public HitBox getHitBox() {
            return hitBox;
        }

        @Override
        public double centerX() {
            return x0 + getTranslateX() + length / 2.0;
        }

        @Override
        public double centerY() {
            return y0 + getTranslateY();
        }

        public void setCollision(CollisionType type) {
            this.collision = type;
        }

        public CollisionType getCollision() {
            return collision;
        }

        @Override
        public void handleCollision(Sprite o) {
            if (getCollision() != o.getCollision() && o.getCollision() != CollisionType.POWER) {
                if (o instanceof SmallPlanet)
                    despawn();
                else
                    delayDespawn(32);
            }
        }

        private void delayDespawn(int delayMillis) {
            Timeline delay = new Timeline(new KeyFrame(Duration.millis(delayMillis)));
            delay.setOnFinished(event -> despawn());
            playTimeline(delay);
        }
    }

    private class DotParticle extends LineParticle {
        public DotParticle(double x0, double y0, double xSpeed, double ySpeed) {
            super(LINE_HEIGHT, x0, y0, xSpeed, ySpeed);
        }
    }

    private class SmallPlanet extends Circle implements Sprite {

        private final CircleHitBox hitBox;
        private int hits = 0;

        public SmallPlanet(int height) { //0 for high, 1 for center, 2 for low
            super(Size.lessWidthHeight(30));
            setFill(Color.BLACK);
            setStroke(Color.WHITE);
            setStrokeWidth(LINE_HEIGHT * 2);
            hitBox = new CircleHitBox(0, 0, Size.lessWidthHeight(30));
            setTranslateX(Size.width(1950));
            hitBox.translateX(Size.width(1950));
            double offset = Size.height(30 + 35 * height);
            setTranslateY(offset);
            hitBox.translateY(offset);
        }

        @Override
        public void spawn(boolean front, int delayMillis, Consumer<Pane> onDespawn) {
            if (!front)
                throw new IllegalArgumentException("Can't place small planet on background");
            frontPlane.getChildren().add(this);
            //60 fps
            Timeline animation = new Timeline(new KeyFrame(Duration.millis(16), event -> {
                double dx = -0.016 * ADVANCE_SPEED;
                this.setTranslateX(getTranslateX() + dx);
                hitBox.translateX(dx);
                if (getTranslateX() < -getRadius())
                    despawn();
            }));
            animation.setDelay(Duration.millis(delayMillis));
            animation.setCycleCount(Animation.INDEFINITE);
            playTimeline(animation);
        }

        @Override
        public void despawn() {
            //there shouldn't be a special event for this object
            frontPlane.getChildren().remove(this);
        }

        @Override
        public HitBox getHitBox() {
            return hitBox;
        }

        @Override
        public double centerX() {
            return getTranslateX();
        }

        @Override
        public double centerY() {
            return getTranslateY();
        }

        @Override
        public CollisionType getCollision() {
            return CollisionType.ALL;
        }


        @Override
        public void handleCollision(Sprite o) {
            if (o.getCollision() == CollisionType.ALLY || o.getCollision() == CollisionType.ENEMY) {
                hits++;
            }
            //we won't actually need to hit it twelve times, bullets have a delayed despawn and often register 2-3 hits each
            if (hits == 8) {
                score(250);
                explode(SpaceGame.this, Size.lessWidthHeight(40));
                despawn();
            }
        }
    }

    private class LargePlanet extends Circle implements Sprite {

        Consumer<Pane> onDespawn;
        final CollisionType collision = CollisionType.ALL;
        private CircleHitBox hitBox;
        private Arc arc;

        LargePlanet(boolean top) {
            super(Size.lessWidthHeight(200));
            setStroke(Color.WHITE);
            setStrokeWidth(Size.lessWidthHeight(3));
            setFill(Color.BLACK);
            this.setTranslateX(Size.width(2000 + getRadius()));
            this.setTranslateY(Size.height(top ? -120 : 250));
            hitBox = new CircleHitBox(getTranslateX(), getTranslateY(), Size.lessWidthHeight(200));

            //cheat the bottom planet so they don't exceed the bounds
            arc = null;
            if (!top) {
                setVisible(false);
                arc = new Arc(this.getCenterX(), this.getCenterY(), this.getRadius(), this.getRadius(), 40.0, 100);
                arc.setFill(Color.BLACK);
                arc.setStroke(Color.WHITE);
                arc.setStrokeWidth(Size.lessWidthHeight(3));
                arc.setTranslateX(getTranslateX());
                arc.setTranslateY(getTranslateY());
                frontPlane.getChildren().add(arc);
            }
            setCache(true);
            setCacheHint(CacheHint.SPEED);
            setCacheShape(true);
        }

        @Override
        public void spawn(boolean front, int delayMillis, Consumer<Pane> onDespawn) {
            if (!front)
                throw new IllegalArgumentException("Can't place large planet on background");
            this.onDespawn = onDespawn;
            frontPlane.getChildren().add(this);
            //60 fps
            Timeline animation = new Timeline(new KeyFrame(Duration.millis(16), event -> {
                double dx = -0.016 * ADVANCE_SPEED;
                this.setTranslateX(getTranslateX() + dx);
                hitBox.translateX(dx);
                if (getTranslateX() < -getRadius())
                    despawn();
                if (arc != null) {
                    arc.setTranslateX(arc.getTranslateX() + dx);
                }
            }));
            animation.setDelay(Duration.millis(delayMillis));
            animation.setCycleCount(Animation.INDEFINITE);
            playTimeline(animation);
        }

        @Override
        public void despawn() {
            //there shouldn't be a special event for this object
            frontPlane.getChildren().remove(this);
        }

        @Override
        public HitBox getHitBox() {
            return hitBox;
        }

        @Override
        public double centerX() {
            return getTranslateX();
        }

        @Override
        public double centerY() {
            return getTranslateY();
        }

        @Override
        public CollisionType getCollision() {
            return collision;
        }

        @Override
        public void handleCollision(Sprite o) {
            //nothing, large planets are invincible.
        }
    }

    private class Explosion extends Pane implements Sprite {

        private final double centerX;
        private final double centerY;

        private Explosion(double centerX, double centerY, double radius) {
            Circle whiteRing = new Circle(Size.lessWidthHeight(radius));
            whiteRing.setStroke(Color.WHITE);
            whiteRing.setStrokeWidth(Size.lessWidthHeight(2));
            getChildren().add(whiteRing);
            Circle whiteCenter = new Circle(Size.lessWidthHeight(radius / 1.5));
            whiteCenter.setFill(Color.WHITE);
            getChildren().add(whiteCenter);

            this.centerX = centerX;
            setTranslateX(Size.width(this.centerX));
            this.centerY = centerY;
            setTranslateY(Size.height(this.centerY));
            setCache(true);
            setCacheShape(true);
        }

        @Override
        public void spawn(boolean front, int delayMillis, Consumer<Pane> onDespawn) {
            if (!front)
                throw new IllegalArgumentException("cannot spawn explosion on back");
            frontPlane.getChildren().add(this);

            Timeline animation = new Timeline(new KeyFrame(Duration.millis(67), new KeyValue(this.opacityProperty(), 0)));
            animation.setAutoReverse(true);
            animation.setCycleCount(4);
            animation.setDelay(Duration.millis(delayMillis));
            Timeline wait = new Timeline(new KeyFrame(Duration.millis(400)));
            wait.setOnFinished(event -> despawn());
            animation.setOnFinished(event -> playTimeline(wait));
            playTimeline(animation);
        }

        @Override
        public void despawn() {
            frontPlane.getChildren().remove(this);
        }

        @Override
        public HitBox getHitBox() {
            return null;
        }

        @Override
        public double centerX() {
            return centerX;
        }

        @Override
        public double centerY() {
            return centerY;
        }

        @Override
        public CollisionType getCollision() {
            return null;
        }

        @Override
        public void handleCollision(Sprite o) {
            //no effect
        }
    }

    private class HUD extends VBox implements Sprite {

        public HUD() {
            Text[] entries = {
                    new Text("Move: Up/Down or W/S"),
                    new Text("Fire: Space"),
                    new Text("Quit: Escape")
            };
            for (Text t : entries) {
                t.setFill(Color.WHITE);
                t.setFont(CustomFonts.sh_pinscher(20));
                t.setTextAlignment(TextAlignment.CENTER);
            }
            getChildren().addAll(entries);
            setTranslateX(Size.width(30));
            setTranslateY(Size.height(20));
            setViewOrder(Integer.MIN_VALUE);
        }

        @Override
        public void spawn(boolean front, int delayMillis, Consumer<Pane> onDespawn) {
            if (!front) throw new IllegalArgumentException("cannot spawn on back pane");
            frontPlane.getChildren().add(this);
            Timeline removeInstructions = new Timeline(new KeyFrame(Duration.millis(1), event -> {
                ((Text) getChildren().get(1)).setText("");
                ((Text) getChildren().get(2)).setText("");
                Text last = new Text("");
                last.setFill(Color.WHITE);
                last.setFont(CustomFonts.sh_pinscher(20));
                last.setTextAlignment(TextAlignment.CENTER);
                getChildren().add(last);
            }));
            removeInstructions.setDelay(Duration.millis(5000));
            playTimeline(removeInstructions);
            removeInstructions.setOnFinished(event -> {
                Timeline updateScore = new Timeline(new KeyFrame(Duration.millis(32), event1 -> {
                    ((Text) getChildren().get(0)).setText(new DecimalFormat("#####000000").format(getScore()));
                    ((Text) getChildren().get(1)).setText("");
                    ((Text) getChildren().get(2)).setText("");
                    ((Text) getChildren().get(3)).setText("");

                    int which = 1;
                    if (ship.rapidFire > 0) {
                        ((Text) getChildren().get(which)).setText("R: 0:" + new DecimalFormat("00").format(ship.rapidFire / 1000 + 1));
                        which++;
                    }
                    if (ship.shield > 0) {
                        ((Text) getChildren().get(which)).setText("S: 0:" + new DecimalFormat("00").format(ship.shield / 1000 + 1));
                        which++;
                    }
                    if (ship.tripleShot > 0) {
                        ((Text) getChildren().get(which)).setText("T: 0:" + new DecimalFormat("00").format(ship.tripleShot / 1000 + 1));
                    }
                }));
                updateScore.setCycleCount(Animation.INDEFINITE);
                playTimeline(updateScore);
            });
        }

        @Override
        public void despawn() {
            frontPlane.getChildren().remove(this);
        }

        @Override
        public HitBox getHitBox() {
            return null;
        }

        @Override
        public double centerX() {
            throw new UnsupportedOperationException("should not explode");
        }

        @Override
        public double centerY() {
            throw new UnsupportedOperationException("should not explode");
        }

        @Override
        public CollisionType getCollision() {
            return null;
        }

        @Override
        public void handleCollision(Sprite o) {
            //no effect
        }
    }

    private double getScore() {
        return score;
    }

    private interface HitBox {
        boolean overlaps(HitBox h);

        double centerX();

        double centerY();

        double xRadius();

        double yRadius();

        void translateX(double dx);

        void translateY(double dy);

        default void translate(double dx, double dy) {
            translateX(dx);
            translateY(dy);
        }
    }

    public class CircleHitBox implements HitBox {

        private double centerX;
        private double centerY;
        private final double radius;

        CircleHitBox(double centerX, double centerY, double radius) {

            this.centerX = centerX;
            this.centerY = centerY;
            this.radius = radius;
        }

        @Override
        public boolean overlaps(HitBox h) {
            if (h instanceof RectangleHitBox)
                return h.overlaps(this);
            else if (h instanceof CircleHitBox) {
                double centerDistance = Math.sqrt(Math.pow(centerX() - h.centerX(), 2) + Math.pow(centerY() - h.centerY(), 2));
                return centerDistance < xRadius() + h.xRadius();
            }
            throw new IllegalArgumentException("undefined hitbox implementation");
        }

        @Override
        public double centerX() {
            return centerX;
        }

        @Override
        public double centerY() {
            return centerY;
        }

        @Override
        public double xRadius() {
            return radius;
        }

        @Override
        public double yRadius() {
            return radius;
        }

        @Override
        public void translateX(double dx) {
            centerX += dx;
        }

        @Override
        public void translateY(double dy) {
            centerY += dy;
        }
    }

    public class RectangleHitBox extends Rectangle implements HitBox {

        private double x, y;
        private double width, height;

        RectangleHitBox(double x, double y, double width, double height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }

        @Override
        public boolean overlaps(HitBox h) {
            if (h instanceof RectangleHitBox) {
                boolean horizontalOverlap = Math.abs(centerX() - h.centerX()) < Math.abs(xRadius() + h.xRadius());
                boolean verticalOverlap = Math.abs(centerY() - h.centerY()) < Math.abs(yRadius() + h.yRadius());
                return horizontalOverlap && verticalOverlap;
            } else if (h instanceof CircleHitBox) {
                //just cheat and use the standard library
                return new Rectangle(x, y, width, height).getBoundsInLocal().intersects(new Circle(h.centerX(), h.centerY(), h.xRadius()).getBoundsInLocal());
            }
            throw new IllegalArgumentException("unsupported hitbox type");
        }

        @Override
        public double centerX() {
            return x + width / 2;
        }

        @Override
        public double centerY() {
            return y + height / 2;
        }

        @Override
        public double xRadius() {
            return width / 2;
        }

        @Override
        public double yRadius() {
            return height / 2;
        }

        @Override
        public void translateX(double dx) {
            x += dx;
        }

        @Override
        public void translateY(double dy) {
            y += dy;
        }
    }

    private class PowerUp extends Pane implements Sprite {


        private final int type;
        private final CircleHitBox hitBox;

        public PowerUp(int type) {
            this.type = type;
            Circle outer = new Circle(Size.lessWidthHeight(20));
            outer.setStrokeWidth(Size.lessWidthHeight(2));
            outer.setStroke(Color.WHITE);
            getChildren().add(outer);
            Circle inner = new Circle(Size.lessWidthHeight(17));
            inner.setStrokeWidth(Size.lessWidthHeight(2));
            inner.setStroke(Color.WHITE);
            getChildren().add(inner);
            Text text = new Text(Character.toString((char) ('R' + type)));
            text.setFont(CustomFonts.sh_pinscher(Size.fontSize(32)));
            text.setFill(Color.WHITE);
            text.setTranslateX(Size.width(-7));
            text.setTranslateY(Size.height(10));
            getChildren().add(text);
            setAlignment(Pos.CENTER_LEFT);
            hitBox = new CircleHitBox(0, 0, Size.lessWidthHeight(20));

            setCache(true);
            setCacheHint(CacheHint.QUALITY);
            setCacheShape(true);
        }

        @Override
        public void spawn(boolean front, int delayMillis, Consumer<Pane> onDespawn) {
            if (!front)
                throw new IllegalArgumentException("not supported add");
            setTranslateX(Size.width(1950));
            setTranslateY(Size.height(65));
            hitBox.translateX(Size.width(1950));
            hitBox.translateY(Size.height(65));
            frontPlane.getChildren().add(this);
            Timeline animation = new Timeline(new KeyFrame(Duration.millis(16), event -> {
                double dx = -ADVANCE_SPEED * 0.016;
                setTranslateX(getTranslateX() + dx);
                hitBox.translateX(dx);
            }));
            animation.setCycleCount(Animation.INDEFINITE);
            animation.setDelay(Duration.millis(delayMillis));
            playTimeline(animation);
        }

        @Override
        public void despawn() {
            frontPlane.getChildren().remove(this);
        }

        @Override
        public HitBox getHitBox() {
            return hitBox;
        }

        @Override
        public double centerX() {
            return getTranslateX();
        }

        @Override
        public double centerY() {
            return getTranslateY();
        }

        @Override
        public CollisionType getCollision() {
            return CollisionType.POWER;
        }

        @Override
        public void handleCollision(Sprite o) {
            if (o == ship)
                despawn();
        }
    }
}
