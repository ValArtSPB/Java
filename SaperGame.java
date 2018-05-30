import javafx.scene.control.Cell;

import javax.swing.*;//рисует стандартные оьъекты в окне
import java.awt.*;
import java.awt.event.*; //обрабатывается action - клик мышкой
import java.util.*;
import java.util.concurrent.BlockingDeque;

class SaperGame extends JFrame {

    final String TITLE_OF_PROGTAM = "Mines";
    final String SIGN_OF_FLAG = "f";
    final int BLOCK_SIZE = 30; //размер блока в пикселях
    final int FIELD_SIZE = 9; //размер поля в блоках
    final int FIELD_DX = 15; // ширина рамок
    final int FIELD_DY = 40; // ширина рамок с учетом заголовка, определяется эксперементально
    final int START_LOCATION = 210; // размер поля в пикселях
    final int MOUSE_BUTTON_LEFT = 1; //для задания клика ЛКМ
    final int MOUSE_BUTTON_RIGHT = 3; // для задания клика ПКМ
    final int NUMBER_OF_MINES = 10; // число мин
    final int[] COLOR_OF_NUMBERS = {0x0000FF, 0x008000, 0xFF0000, 0x80000, 0x0}; //цвета цифр в блочках
    Cell[][] field = new Cell[FIELD_SIZE][FIELD_SIZE];
    Random random = new Random();
    int countOpenedCells; // количество открытых ячеек
    boolean youWin, bangMine; //победа/поражение
    int bangX, bangY; // координаты взрыва

    public static void main(String[] args) {
        new SaperGame(); //строка стартует игру
    }

    SaperGame() { // конструктор класса игры
        setTitle(TITLE_OF_PROGTAM); //устанавливаем название окна
        setDefaultCloseOperation(EXIT_ON_CLOSE); // закрытие программы при нажатии на крестик окна
        setBounds(START_LOCATION, START_LOCATION, FIELD_SIZE*BLOCK_SIZE + FIELD_DX, FIELD_SIZE*BLOCK_SIZE + FIELD_DY); // стартовая позиция окна, размеры окна
        setResizable(false); // запрещает возможность масштабировать окно
        final Canvas canvas = new Canvas();
        canvas.setBackground(Color.white); // задаем фон
        canvas.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) { // переопределяем нажатие на кнопку мыши
                super.mouseReleased(e); // вызываем родительский класс мыши
                int x = e.getX() / BLOCK_SIZE; //вызываем абсолютные координаты клика мыши. Разделив на размер блока получаем относительные
                int y = e.getY() / BLOCK_SIZE;
                //if (!bangMine && !youWin)
                if (e.getButton() == MOUSE_BUTTON_LEFT && !bangMine && !youWin) //если нажата ЛКП и мина не взорвалась и не победили
                    if (field[y][x].isNotOpen()) { //проверка не открыта ли ячейка
                        openCells(x, y);
                        youWin = countOpenedCells == FIELD_SIZE * FIELD_SIZE - NUMBER_OF_MINES; // победа, если размеры поля по блоках - число бомб = количеству открытых ячеек
                        //check
                        if (bangMine) { // если взорвались, запоминаем координаты взрыва
                            bangX = x;
                            bangY = y;
                        }
                    }
                if (e.getButton() == MOUSE_BUTTON_RIGHT) field[y][x].inverseFlag();
                    canvas.repaint();
            }
        }); // прослушиватель нажатия на мышь
        add(BorderLayout.CENTER, canvas);
        setVisible(true);
        initField();
    }

    void openCells (int x, int y) {
            if (x < 0 || x > FIELD_SIZE - 1 || y < 0 || y > FIELD_SIZE -1) return; // проверка плохих координат
            if (!field[y][x].isNotOpen()) return; // проверка - ячейка уже открыта
            field[y][x].open();
            if (field[y][x].getCountBomb() > 0 || bangMine) return; // если ячейка не пустая
            for (int dx = -1; dx < 2; dx++)
                for (int dy = -1; dy < 2; dy++) openCells (x + dx , y + dy);
        }

    void initField() { // инициализируем игровое поле
    int x,y, countMines = 0;
    for (x = 0; x < FIELD_SIZE; x++) // создаем ячейки
        for (y = 0; y < FIELD_SIZE; y++)
            field[x][y] = new Cell();
    while (countMines < NUMBER_OF_MINES) { //расставляем мины
        do {
            x = random.nextInt(FIELD_SIZE);
            y = random.nextInt(FIELD_SIZE);
        }

            while (field[x][y].isMined()) ;
            field[x][y].mine();
            countMines++;
        }
        for (x = 0; x < FIELD_SIZE; x++) //цыфры в ячейках около мин. Разобраться досконально
            for (y = 0; y < FIELD_SIZE; y++)
                if (!field[y][x].isMined()) {
                    int count = 0;
                    for(int dx = -1; dx < 2; dx++)
                        for (int dy = -1; dy < 2; dy++) {
                        int nX = x + dx;
                        int nY = y + dy;
                        if (nX <0 || nY < 0 || nX > FIELD_SIZE - 1 || nY > FIELD_SIZE -1) {
                            nX = x;
                            nY = y;
                        }
                        count += (field[nY][nX].isMined()) ? 1 : 0;
                        }
                     field[y][x].setCountBomb(count);
                }
    }

    class Cell {
       private boolean isOpen, isMine, isFlag;
       private int countBombNear;

        void open() {
            isOpen = true;
            bangMine = isMine;
            if(!isMine) countOpenedCells++;
        }
        void mine(){
            isMine = true;
        }

        boolean isNotOpen() {
            return !isOpen;
        }

        void inverseFlag() {
            isFlag = !isFlag;
        }
        boolean isMined() {
            return isMine;
        }

        void setCountBomb (int count) {
            countBombNear = count;
        }
        int getCountBomb () {
            return countBombNear;
        }
        void paintBomb(Graphics g, int x, int y, Color color) {
            g.setColor(color);
            g.fillRect(x*BLOCK_SIZE +7 , y*BLOCK_SIZE + 10, 18,10);
            g.fillRect(x*BLOCK_SIZE + 11, y*BLOCK_SIZE + 6, 10, 18);
            g.fillRect(x*BLOCK_SIZE+9,y*BLOCK_SIZE+8, 14,14);
            g.setColor(Color.white);
            g.fillRect(x*BLOCK_SIZE + 11, y* BLOCK_SIZE + 10, 4, 4);
        }
        void paintString (Graphics g,String str, int x, int y, Color color) {
            g.setColor(color);
            g.setFont((new Font("", Font.BOLD, BLOCK_SIZE)));
            g.drawString(str, x*BLOCK_SIZE+8, y*BLOCK_SIZE + 26);
        }
        void paint (Graphics g, int x, int y) {
            g.setColor(Color.lightGray);
            g.drawRect(x*BLOCK_SIZE, y*BLOCK_SIZE, BLOCK_SIZE, BLOCK_SIZE);
            if (!isOpen) {
                if ((bangMine || youWin) && isMine) paintBomb(g,x,y,Color.BLACK);
                else {
                    g.setColor(Color.lightGray);
                    g.fill3DRect(x*BLOCK_SIZE,y*BLOCK_SIZE, BLOCK_SIZE, BLOCK_SIZE, true);
                    if (isFlag) paintString(g, SIGN_OF_FLAG, x, y, Color.red);
                }
            } else
                if (isMine) paintBomb(g,x ,y, bangMine? Color.red : Color.BLACK);
                else
                    if (countBombNear > 0)
                        paintString(g, Integer.toString(countBombNear),x,y,new Color(COLOR_OF_NUMBERS[countBombNear-1]));

        }
    }

    class Canvas extends JPanel {
            public void paint (Graphics g) {
                super.paint(g);
                for (int x = 0; x < FIELD_SIZE; x++)
                    for (int y  = 0 ; y < FIELD_SIZE; y++) field[y][x].paint(g,x,y);
            }
    }
}
