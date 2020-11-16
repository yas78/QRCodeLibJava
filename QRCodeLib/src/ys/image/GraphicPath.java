package ys.image;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GraphicPath {
    private enum Direction {
        UP, DOWN, LEFT, RIGHT,
    }

    public static Point[][] FindContours(int[][] image) {
        List<Point[]> paths = new ArrayList<Point[]>();

        for (int y = 0; y < image.length - 1; y++) {
            for (int x = 0; x < image[y].length - 1; x++) {
                if (image[y][x] == Integer.MAX_VALUE) {
                    continue;
                }

                if (!(image[y][x] > 0 && image[y][x + 1] <= 0)) {
                    continue;
                }

                image[y][x] = Integer.MAX_VALUE;
                Point start = new Point(x, y);
                List<Point> path = new ArrayList<Point>(Arrays.asList(start));

                Direction dr = Direction.UP;
                Point p = new Point(start.x, start.y - 1);

                do {
                    switch (dr) {
                    case UP:
                        if (image[p.y][p.x] > 0) {
                            image[p.y][p.x] = Integer.MAX_VALUE;

                            if (image[p.y][p.x + 1] <= 0)
                                p = new Point(p.x, p.y - 1);
                            else {
                                path.add(p);
                                dr = Direction.RIGHT;
                                p = new Point(p.x + 1, p.y);
                            }
                        } else {
                            p = new Point(p.x, p.y + 1);
                            path.add(p);
                            dr = Direction.LEFT;
                            p = new Point(p.x - 1, p.y);
                        }
                        break;

                    case DOWN:
                        if (image[p.y][p.x] > 0) {
                            image[p.y][p.x] = Integer.MAX_VALUE;

                            if (image[p.y][p.x - 1] <= 0)
                                p = new Point(p.x, p.y + 1);
                            else {
                                path.add(p);
                                dr = Direction.LEFT;
                                p = new Point(p.x - 1, p.y);
                            }
                        } else {
                            p = new Point(p.x, p.y - 1);
                            path.add(p);
                            dr = Direction.RIGHT;
                            p = new Point(p.x + 1, p.y);
                        }
                        break;

                    case LEFT:
                        if (image[p.y][p.x] > 0) {
                            image[p.y][p.x] = Integer.MAX_VALUE;

                            if (image[p.y - 1][p.x] <= 0)
                                p = new Point(p.x - 1, p.y);
                            else {
                                path.add(p);
                                dr = Direction.UP;
                                p = new Point(p.x, p.y - 1);
                            }
                        } else {
                            p = new Point(p.x + 1, p.y);
                            path.add(p);
                            dr = Direction.DOWN;
                            p = new Point(p.x, p.y + 1);
                        }
                        break;

                    case RIGHT:
                        if (image[p.y][p.x] > 0) {
                            image[p.y][p.x] = Integer.MAX_VALUE;

                            if (image[p.y + 1][p.x] <= 0)
                                p = new Point(p.x + 1, p.y);
                            else {
                                path.add(p);
                                dr = Direction.DOWN;
                                p = new Point(p.x, p.y + 1);
                            }
                        } else {
                            p = new Point(p.x - 1, p.y);
                            path.add(p);
                            dr = Direction.UP;
                            p = new Point(p.x, p.y - 1);
                        }
                        break;

                    default:
                        throw new IllegalStateException();
                    }

                } while (!(p.equals(start)));

                paths.add(path.toArray(new Point[path.size()]));
            }
        }

        return paths.toArray(new Point[paths.size()][]);
    }
}
