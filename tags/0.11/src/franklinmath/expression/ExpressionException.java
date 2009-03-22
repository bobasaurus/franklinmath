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

package franklinmath.expression;

/**
 * An exception related to expressions.  
 * @author Allen Jordan
 */
public class ExpressionException extends Exception {
    protected String msg;
    
    public ExpressionException() {
        msg = "";
    }
    public ExpressionException(String message) {
        msg = message;
    }
    
    @Override public String getMessage() {
        return msg;
    }
    
    @Override public String toString() {
        return getMessage();
    }
}
