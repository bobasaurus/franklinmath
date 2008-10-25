package franklinmath.expression;

import java.util.*;

import franklinmath.util.*;

/**
 * This class represents an immutable math power.  Factors to the power of other factors.  
 * @author Allen Jordan
 */
public final class Power implements LatexOutput {

    final private Vector<Factor> factorList;

    public Power() {
        factorList = new Vector<Factor>();
    }

    public Power(Factor factor) {
        assert factor != null;
        factorList = new Vector<Factor>();
        factorList.add(factor);
    }

    public Power(Vector<Factor> inputFactorList) {
        assert inputFactorList != null;
        factorList = (Vector<Factor>) inputFactorList.clone();
    }

    public Power(Vector<Factor> inputFactorList, Factor insertFactor) {
        assert ((inputFactorList != null) && (insertFactor != null));
        factorList = (Vector<Factor>) inputFactorList.clone();
        factorList.add(insertFactor);
    }

    public Power(Vector<Factor> inputFactorList, int index, Factor newFactor, boolean isInsertion) {
        assert ((inputFactorList != null) && (newFactor != null));
        assert ((index >= 0) && (index < inputFactorList.size()));
        factorList = (Vector<Factor>) inputFactorList.clone();
        if (isInsertion) {
            factorList.insertElementAt(newFactor, index);
        } else {
            factorList.set(index, newFactor);
        }
    }

    public Power AppendFactor(Factor factor) {
        return new Power(factorList, factor);
    }

    public int NumFactors() {
        return factorList.size();
    }

    public Power RemoveFactor(int index) throws ExpressionException {
        if ((index < 0) || (index >= factorList.size())) {
            throw new ExpressionException("Removal index out of range");
        }
        Vector<Factor> factorListCopy = (Vector<Factor>) factorList.clone();
        factorListCopy.remove(index);

        return new Power(factorListCopy);
    }

    public Power ReplaceFactor(int index, Factor newFactor) throws ExpressionException {
        if ((index < 0) || (index >= factorList.size())) {
            throw new ExpressionException("Removal index out of range");
        }
        return new Power(factorList, index, newFactor, false);
    }

    public int FindFactor(Factor factor) {
        return factorList.indexOf(factor);
    }

    public Factor GetFactor(int index) throws ExpressionException {
        if ((index < 0) || (index >= factorList.size())) {
            throw new ExpressionException("Retrieval index out of range");
        }
        return factorList.get(index);
    }

    public Vector<Factor> GetFactors() {
        return (Vector<Factor>) factorList.clone();
    }

    public Factor GetSingleFactor() {
        if (factorList.size() != 1) {
            return null;
        }
        return factorList.get(0);
    }

    public String toLatexString() {
        return "";
    }

    @Override
    public String toString() {
        StringBuilder strBuilder = new StringBuilder();
        int factorListSize = factorList.size();
        for (int i = 0; i < factorListSize; i++) {
            Factor factor = factorList.get(i);

            if (i != 0) {
                strBuilder.append("^");
            }
            strBuilder.append(factor.toString());
        }
        return strBuilder.toString();
    }

    /**
     * Check equality of Power objects.  Powers are only equal if they have the same number of factors, and each factor is equal.  
     * @param obj   The Power object to compare against this one.  
     * @return      The result of the equality comparison.  
     */
    @Override
    public boolean equals(Object obj) {
        //check for equal reference values
        if (this == obj) {
            return true;
        }

        try {
            Power comparePower = (Power) obj;
            assert factorList.size() > 0;

            if (comparePower.NumFactors() != factorList.size()) {
                return false;
            }

            int listSize = factorList.size();
            for (int i = 0; i < listSize; i++) {
                if (!comparePower.GetFactor(i).equals(factorList.get(i))) {
                    return false;
                }
            }
        } catch (ExpressionException ex) {
            return false;
        }

        return true;
    }

    /**
     * Generate a hash code for this Power object.  
     * @return  The hash code.  
     */
    @Override
    public int hashCode() {
        int primeNumber = 7;
        int hash = 1;

        int listSize = factorList.size();
        for (int i = 0; i < listSize; i++) {
            hash = hash * primeNumber + factorList.get(i).hashCode();
        }

        return hash;
    }
}
