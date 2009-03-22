/*
Copyright 2009 Allen Franklin Jordan (allen.jordan@gmail.com).

This file is part of Franklin Math.

Franklin Math is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Franklin Math is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Franklin Math.  If not, see <http://www.gnu.org/licenses/>.
*/

package franklinmath.util;

/**
 * Class representing a single 2-dimensional point.  
 * @author Allen Jordan
 */
public class Point {
    //For ease of access, just expose these as public.  
    public double x,  y;

    public Point() {
        x = 0;
        y = 0;
    }

    public Point(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public Point(Point copyPoint) {
        x = copyPoint.x;
        y = copyPoint.y;
    }
    public static Point BAD_POINT = new Point(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);

    @Override
    public String toString() {
        return x + "," + y;
    }
}
