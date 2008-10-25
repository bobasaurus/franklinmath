package franklinmath.expression;

import java.util.*;

import franklinmath.util.*;

/**
 * This class represents an immutable math term.  Powers are multiplied or divided.  
 * @author Allen Jordan
 */
public final class Term implements LatexOutput {

    final private Vector<Power> powerList;
    final private Vector<PowerOperator> operatorList;

    public Term() {
        powerList = new Vector<Power>();
        operatorList = new Vector<PowerOperator>();
    }

    public Term(Power power) {
        assert power != null;
        powerList = new Vector<Power>();
        operatorList = new Vector<PowerOperator>();
        powerList.add(power);
        operatorList.add(PowerOperator.NONE);
    }

    public Term(Vector<Power> inputPowerList, Vector<PowerOperator> inputOperatorList) {
        assert ((inputPowerList != null) && (inputOperatorList != null));
        assert (inputPowerList.size() == inputOperatorList.size());
        powerList = (Vector<Power>) inputPowerList.clone();
        operatorList = (Vector<PowerOperator>) inputOperatorList.clone();
    }

    public Term(Vector<Power> inputPowerList, Vector<PowerOperator> inputOperatorList, Power appendPower, PowerOperator appendOperator) throws ExpressionException {
        assert ((inputPowerList != null) && (inputOperatorList != null) && (appendPower != null) && (appendOperator != null));
        assert (inputPowerList.size() == inputOperatorList.size());
        powerList = (Vector<Power>) inputPowerList.clone();
        operatorList = (Vector<PowerOperator>) inputOperatorList.clone();

        if (powerList.size() == 0) {
            if (appendOperator == PowerOperator.MULTIPLY) {
                appendOperator = PowerOperator.NONE;
            } else if (appendOperator == PowerOperator.DIVIDE) {
                throw new ExpressionException("Division as first power operator in term");
            }
        } else {
            if (appendOperator == PowerOperator.NONE) {
                appendOperator = PowerOperator.MULTIPLY;
            }
        }

        powerList.add(appendPower);
        operatorList.add(appendOperator);
    }

    public Term(Vector<Power> inputPowerList, Vector<PowerOperator> inputOperatorList, int index, Power newPower, PowerOperator newOperator, boolean isInsertion) throws ExpressionException {
        assert ((inputPowerList != null) && (inputOperatorList != null) && (newPower != null) && (newOperator != null));
        assert (inputPowerList.size() == inputOperatorList.size());
        assert ((index >= 0) && (index < inputPowerList.size()));
        powerList = (Vector<Power>) inputPowerList.clone();
        operatorList = (Vector<PowerOperator>) inputOperatorList.clone();

        if (powerList.size() == 0) {
            if (newOperator == PowerOperator.MULTIPLY) {
                newOperator = PowerOperator.NONE;
            } else if (newOperator == PowerOperator.DIVIDE) {
                throw new ExpressionException("Division as first power operator in term");
            }
        }
        if (index == 0) {
            if (newOperator == PowerOperator.MULTIPLY) {
                newOperator = PowerOperator.NONE;
            } else if (newOperator == PowerOperator.DIVIDE) {
                throw new ExpressionException("Division as first power operator in term");
            }
        } else {
            if (newOperator == PowerOperator.NONE) {
                newOperator = PowerOperator.MULTIPLY;
            }
        }

        if (isInsertion) {
            powerList.insertElementAt(newPower, index);
            operatorList.insertElementAt(newOperator, index);
        } else {
            powerList.set(index, newPower);
            operatorList.set(index, newOperator);
        }
    }

    public Term AppendPower(Power power, PowerOperator operator) throws ExpressionException {
        return new Term(powerList, operatorList, power, operator);
    }

    public int NumPowers() {
        return powerList.size();
    }

    public Term RemovePower(int index) throws ExpressionException {
        if ((index < 0) || (index >= powerList.size())) {
            throw new ExpressionException("Removal index out of range");
        }
        
        Vector<Power> powerListCopy = (Vector<Power>) powerList.clone();
        Vector<PowerOperator> opListCopy = (Vector<PowerOperator>) operatorList.clone();
        powerListCopy.remove(index);
        opListCopy.remove(index);
        
        return new Term(powerListCopy, opListCopy);
    }

    public Term ReplacePower(int index, Power newPower, PowerOperator newOperator) throws ExpressionException {
        if ((index < 0) || (index >= powerList.size())) {
            throw new ExpressionException("Replacement index out of range");
        }
        
        return new Term(powerList, operatorList, index, newPower, newOperator, false);
    }

    public int FindPower(Power power) {
        return powerList.indexOf(power);
    }

    public Power GetPower(int index) throws ExpressionException {
        if ((index < 0) || (index >= powerList.size())) {
            throw new ExpressionException("Power retrieval index out of range");
        }
        return powerList.get(index);
    }

    public PowerOperator GetOperator(int index) throws ExpressionException {
        if ((index < 0) || (index >= operatorList.size())) {
            throw new ExpressionException("Power operator retrieval index out of range");
        }
        return operatorList.get(index);
    }

    public Vector<Power> GetPowers() {
        return (Vector<Power>) powerList.clone();
    }

    public Vector<PowerOperator> GetOperators() {
        return (Vector<PowerOperator>) operatorList.clone();
    }

    public Factor GetSingleFactor() {
        if (powerList.size() != 1) {
            return null;
        }
        return powerList.get(0).GetSingleFactor();
    }
    
    public String toLatexString() {
        return "";
    }

    @Override
    public String toString() {
        StringBuilder strBuilder = new StringBuilder();
        int powerListSize = powerList.size();
        for (int i = 0; i < powerListSize; i++) {
            PowerOperator powerOp = operatorList.get(i);
            Power power = powerList.get(i);

            if (powerOp.compareTo(PowerOperator.DIVIDE) == 0) {
                if (i != 0) {
                    strBuilder.append("/");
                } else {
                    strBuilder.append("Error: division operator in front of term");
                }
            } else if (powerOp.compareTo(PowerOperator.MULTIPLY) == 0) {
                if (i != 0) {
                    strBuilder.append("*");
                }
            }

            strBuilder.append(power.toString());
        }

        return strBuilder.toString();
    }

    @Override
    public boolean equals(Object obj) {
        //check for equal reference values
        if (this == obj) {
            return true;
        }

        try {
            Term compareTerm = (Term) obj;

            assert powerList.size() == operatorList.size();
            assert compareTerm.NumPowers() == compareTerm.GetOperators().size();

            int numPowers = powerList.size();
            int numComparePowers = compareTerm.NumPowers();

            //check for a match (regardless of power order)
            Hashtable<Power, Integer> powerMulTable = new Hashtable<Power, Integer>();
            Hashtable<Power, Integer> powerDivTable = new Hashtable<Power, Integer>();
            for (int i = 0; i < numPowers; i++) {
                Power power = powerList.get(i);
                PowerOperator powerOp = operatorList.get(i);
                if (powerOp.compareTo(PowerOperator.DIVIDE) == 0) {
                    Integer currentValue = powerDivTable.get(power);
                    if (currentValue == null) {
                        currentValue = 0;
                    }
                    currentValue++;
                    powerDivTable.put(power, currentValue);
                } else {
                    Integer currentValue = powerMulTable.get(power);
                    if (currentValue == null) {
                        currentValue = 0;
                    }
                    currentValue++;
                    powerMulTable.put(power, currentValue);
                }
            }
            for (int i = 0; i < numComparePowers; i++) {
                Power power = compareTerm.GetPower(i);
                PowerOperator powerOp = compareTerm.GetOperator(i);
                if (powerOp.compareTo(PowerOperator.DIVIDE) == 0) {
                    Integer currentValue = powerDivTable.get(power);
                    if (currentValue == null) {
                        return false;
                    }
                    currentValue--;
                    powerDivTable.put(power, currentValue);
                } else {
                    Integer currentValue = powerMulTable.get(power);
                    if (currentValue == null) {
                        return false;
                    }
                    currentValue--;
                    powerMulTable.put(power, currentValue);
                }

            }
            Enumeration<Power> mulKeyEnumeration = powerMulTable.keys();
            while (mulKeyEnumeration.hasMoreElements()) {
                if (powerMulTable.get(mulKeyEnumeration.nextElement()) != 0) {
                    return false;
                }
            }
            Enumeration<Power> divKeyEnumeration = powerDivTable.keys();
            while (divKeyEnumeration.hasMoreElements()) {
                if (powerDivTable.get(divKeyEnumeration.nextElement()) != 0) {
                    return false;
                }
            }
            return true;

        } catch (ExpressionException ex) {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int primeNumber = 7;
        int hash = 1;

        assert powerList.size() == operatorList.size();

        //make sure that the power order does not matter in hash code generation
        int listSize = powerList.size();
        for (int i = 0; i < listSize; i++) {
            hash += powerList.get(i).hashCode() * primeNumber;
            if (operatorList.get(i).compareTo(PowerOperator.DIVIDE) == 0) {
                hash++;
            }
        }

        return hash;
    }
}
