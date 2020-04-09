package main.impl;

import lombok.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Stack;

@RequiredArgsConstructor
public class BST<K, T extends BST.NodeItem<K>> {
    private final Comparator<K> comparator;
    private Node<K, T> root;
    private int count = 0;

    public void insert(T value){
        insertRec(root, value);
        count ++;
    }

    public T search(K key){
        Node<K, T> resultNode = search(root, key);
        return (resultNode == null) ? null : resultNode.getData();
    }

    public int getCount(){
        return count;
    }

    public List<T> getAll(){
        List<T> result = new ArrayList<>();
        Stack<Node<K, T>> nodes = new Stack<>();
        nodes.push(root);
        while (!nodes.isEmpty()) {
            Node<K, T> current = nodes.pop();
            result.add(current.data);
            if (current.right != null) {
                nodes.push(current.right);
            } if (current.left != null) {
                nodes.push(current.left);
            }
        }
        return result;
    }

    private Node<K, T> insertRec(Node<K, T> root, T value) {
        if (root == null) {
            return new Node<>(value, null, null);
        }
        if (comparator.compare(value.getID(), root.getData().getID()) < 0)
            root.left = insertRec(root.left, value);
        else if (comparator.compare(value.getID(), root.getData().getID()) < 0){
            root.right = insertRec(root.right, value);
        }
        return root;
    }

    private Node<K, T> search(Node<K, T> root, K key){
        if ( root == null){
            return null;
        }
        int compareValue = comparator.compare(key, root.getData().getID());
        if (compareValue == 0){
            return root;
        }else if (compareValue < 0){
            return search(root.left, key);
        }else{
            return search(root.right, key);
        }
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    @Builder
    private static final class Node<K, T extends NodeItem<K>>{
        T data;
        Node<K, T> left;
        Node<K, T> right;
    }

    public interface NodeItem<T>{
        T getID();
    }
}
