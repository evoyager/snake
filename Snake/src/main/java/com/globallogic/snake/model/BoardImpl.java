package com.globallogic.snake.model;

import com.globallogic.snake.model.artifacts.Apple;
import com.globallogic.snake.model.artifacts.ArtifactGenerator;
import com.globallogic.snake.model.artifacts.Element;
import com.globallogic.snake.model.artifacts.EmptySpace;
import com.globallogic.snake.model.artifacts.Point;
import com.globallogic.snake.model.artifacts.Stone;
import com.globallogic.snake.model.artifacts.Wall;

public class BoardImpl implements Board {

	private Snake snake;
    private Walls walls;
	private Stone stone;
	private int size;
	private Apple apple;
    private SnakeFactory snakeFactory;
    private ArtifactGenerator generator;

    public BoardImpl(ArtifactGenerator generator, int size) {
        this(generator, new SnakeFactory() {
            @Override
            public Snake create(int x, int y) {
                return new Snake(x, y);
            }
        }, size);
    }

    public BoardImpl(ArtifactGenerator generator, SnakeFactory snakeFactory, int size) {
	    this.generator = generator;
	    this.snakeFactory = snakeFactory;
	    this.size = size;
		if (size%2 == 0) {
			throw new IllegalArgumentException();
		}
        walls = new Walls();

        newGame();
	}

    /**
	 * метод настраивает систему так, что при съедании одного яблока автоматически появляется другое.
	 */
	private void generateNewApple() {
		apple = generator.generateApple(snake, stone, size);		
		apple.onEat(new Runnable() {
            @Override
            public void run() {
                generateNewApple();
            }
        });
	}

    // аналогично с камнем - только схели - он сразу появился в другом месте.
    private void generateNewStone() {
        stone = generator.generateStone(snake, apple, size);
        stone.onEat(new Runnable() {
            @Override
            public void run() {
                generateNewStone();
            }
        });
    }

	public Snake getSnake() {
		return snake; 
	}

	public Stone getStone() {  		
		return stone;				
	}

    public Walls getWalls() {
        return walls;
    }

	public void tact() {
		snake.checkAlive();		
		snake.walk(this);
	}

	public Element getAt(Point point) {
		if (stone.itsMe(point)) {
			return stone; 
		}
		
		if (apple.itsMe(point)) {
			return apple;
		}
		
		// получается я свой хвост немогу укусить, потому как я за ним двинусь а он отползет
		// вроде логично
		if (snake.itsMyTail(point)) { 
			return new EmptySpace(point);
		}		
		
		if (snake.itsMyBody(point)) {
			return snake;
		}
		
		if (isWall(point)) {
			return new Wall(point);
		}
		
		return new EmptySpace(point);
	}

    @Override
    public void newGame() {
        int position = (size - 1)/2;
        snake = snakeFactory.create(position, position);
        generateNewStone();
        generateNewApple();
    }

    private boolean isWall(Point point) {
		return point.getX() < 0 || point.getY() < 0 || point.getY() >= size || point.getX() >= size || walls.itsMe(point);
	}

	public boolean isGameOver() {
		return !snake.isAlive();
	}

	public Apple getApple() {
		return apple;
	}

	public int getSize() {
		return size;
	}
}