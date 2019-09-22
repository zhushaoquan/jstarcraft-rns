package com.jstarcraft.rns.model.collaborative.rating;

import java.util.Iterator;

import com.jstarcraft.ai.data.DataInstance;
import com.jstarcraft.ai.math.structure.vector.SparseVector;
import com.jstarcraft.ai.math.structure.vector.VectorScalar;
import com.jstarcraft.rns.model.collaborative.ItemKNNModel;

/**
 * 
 * Item KNN推荐器
 * 
 * <pre>
 * 参考LibRec团队
 * </pre>
 * 
 * @author Birdy
 *
 */
public class ItemKNNRatingModel extends ItemKNNModel {

    @Override
    public void predict(DataInstance instance) {
        int userIndex = instance.getQualityFeature(userDimension);
        int itemIndex = instance.getQualityFeature(itemDimension);
        SparseVector userVector = userVectors[userIndex];
        int[] neighbors = itemNeighbors[itemIndex];
        if (userVector.getElementSize() == 0 || neighbors == null) {
            instance.setQuantityMark(meanScore);
            return;
        }

        float sum = 0F, absolute = 0F;
        int count = 0;
        int leftCursor = 0, rightCursor = 0, leftSize = userVector.getElementSize(), rightSize = neighbors.length;
        Iterator<VectorScalar> iterator = userVector.iterator();
        VectorScalar term = iterator.next();
        // 判断两个有序数组中是否存在相同的数字
        while (leftCursor < leftSize && rightCursor < rightSize) {
            if (term.getIndex() == neighbors[rightCursor]) {
                count++;
                double similarity = symmetryMatrix.getValue(itemIndex, neighbors[rightCursor]);
                double score = term.getValue();
                sum += similarity * (score - itemMeans.getValue(neighbors[rightCursor]));
                absolute += Math.abs(similarity);
                if (iterator.hasNext()) {
                    term = iterator.next();
                }
                leftCursor++;
                rightCursor++;
            } else if (term.getIndex() > neighbors[rightCursor]) {
                rightCursor++;
            } else if (term.getIndex() < neighbors[rightCursor]) {
                if (iterator.hasNext()) {
                    term = iterator.next();
                }
                leftCursor++;
            }
        }

        if (count == 0) {
            instance.setQuantityMark(meanScore);
            return;
        }

        instance.setQuantityMark(absolute > 0 ? itemMeans.getValue(itemIndex) + sum / absolute : meanScore);
    }

}
