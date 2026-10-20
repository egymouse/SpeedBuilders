package org.egymouse.speedBuilders;

import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Stairs;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class StairsVisualComparator {
    public static Map<StatePair,Integer> id = new HashMap<>(){{
        put(new StatePair(BlockFace.EAST, Stairs.Shape.INNER_LEFT),1);
        put(new StatePair(BlockFace.NORTH, Stairs.Shape.INNER_RIGHT),1);
        put(new StatePair(BlockFace.EAST, Stairs.Shape.INNER_RIGHT),2);
        put(new StatePair(BlockFace.SOUTH, Stairs.Shape.INNER_LEFT),2);
        put(new StatePair(BlockFace.EAST, Stairs.Shape.OUTER_LEFT),3);
        put(new StatePair(BlockFace.NORTH, Stairs.Shape.OUTER_RIGHT),3);
        put(new StatePair(BlockFace.EAST, Stairs.Shape.OUTER_RIGHT),4);
        put(new StatePair(BlockFace.SOUTH, Stairs.Shape.OUTER_LEFT),4);
        put(new StatePair(BlockFace.WEST, Stairs.Shape.INNER_LEFT),5);
        put(new StatePair(BlockFace.SOUTH, Stairs.Shape.INNER_RIGHT),5);
        put(new StatePair(BlockFace.WEST, Stairs.Shape.INNER_RIGHT),6);
        put(new StatePair(BlockFace.NORTH, Stairs.Shape.INNER_LEFT),6);
        put(new StatePair(BlockFace.WEST, Stairs.Shape.OUTER_LEFT),7);
        put(new StatePair(BlockFace.SOUTH, Stairs.Shape.OUTER_RIGHT),7);
        put(new StatePair(BlockFace.WEST, Stairs.Shape.OUTER_RIGHT),8);
        put(new StatePair(BlockFace.NORTH, Stairs.Shape.OUTER_LEFT),8);
        put(new StatePair(BlockFace.EAST, Stairs.Shape.STRAIGHT),9);
        put(new StatePair(BlockFace.SOUTH, Stairs.Shape.STRAIGHT),10);
        put(new StatePair(BlockFace.NORTH, Stairs.Shape.STRAIGHT),11);
        put(new StatePair(BlockFace.WEST, Stairs.Shape.STRAIGHT),12);
    }};
    public static boolean Compare(BlockData d1,BlockData d2){
        if(d1 instanceof Stairs s1 && d2 instanceof  Stairs s2){
            if(s1.isWaterlogged() != s2.isWaterlogged()){
                return false;
            }
            if(s1.getHalf() != s2.getHalf()){
                return false;
            }
            if(s1.getMaterial() != s2.getMaterial()){
                return false;
            }
            return Objects.equals(id.get(new StatePair(s1.getFacing(), s1.getShape())), id.get(new StatePair(s2.getFacing(), s2.getShape())));
        }else{
            return false;
        }
    }
    public static class StatePair{
        public BlockFace face;
        public Stairs.Shape shape;
        public StatePair(BlockFace face, Stairs.Shape shape){
            this.face = face;
            this.shape = shape;
        }

        @Override
        public boolean equals(Object obj) {
            if(obj instanceof  StatePair p){
                return p.face == face && p.shape == shape;
            }else{
                return false;
            }
        }
        @Override
        public int hashCode() {
            return Objects.hash(face, shape);   // ← 关键：必须重写
        }
    }
}