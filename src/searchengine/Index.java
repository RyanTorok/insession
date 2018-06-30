package searchengine;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Index {

    private static final long serialVersionUID = 1000L;


    // TODO: Implement all of this! You may choose your own data structures an internal APIs.
    // You should not need to worry about serialization (just make any other data structures you use
    // here also serializable - the Java standard library data structures already are, for example).

    private HashMap<String, ItemTree> index;

    Index() {
        index = new HashMap<>();
    }

    //called by WebCrawler to establish a link between a keyword and a page
    void associate(String word, Indexable item, int relevance) {
        ItemTree tree = index.get(word);
        if (tree == null) {
            ItemTree newTree = new ItemTree();
            newTree.update(item, relevance);
            index.put(word, newTree);
        } else {
            tree.update(item, relevance);
        }
    }

    HashSet<ItemNode> getItems(String word) {
        ItemTree tree = index.get(word);
        if (tree == null)
            return new HashSet<>();
        return tree.getAll();
    }

    int find(String word, Indexable item) {
        ItemTree tree = index.get(word);
        if (tree == null)
            return -1;
        return tree.find(item);
    }

    //O(n) return of all urls in worst case when implicitly negative query reaches the top of the query tree.
    Set<Indexable> getAll() {
        HashSet<Indexable> items = new HashSet<>();
        for (Map.Entry<String, ItemTree> tree : index.entrySet()) {
            HashSet<ItemNode> temp = tree.getValue().getAll();
            for (ItemNode node : temp) {
                //items.add(node.getIdentifier());
            }
        }
        return items;
    }

    static class ItemTree implements Serializable {
        private static final long serialVersionUID = 303L;
        private HashMap<String, ItemTree> children;
        //stores the actual url object if the key node is a valid url
        private Indexable item;
        private String key = null;
        private boolean isItem = false;
        private int relevance;

        public ItemTree() {
            children = new HashMap<>();
        }

        private ItemTree(String key, boolean isItem) {
            this();
            this.key = key;
            this.isItem = isItem;
        }

        void update(Indexable item, int relevance) {
            String idString = item.getUniqueIdentifier();
            //chop off ending slash if necessary
            if (idString.charAt(idString.length() - 1) == '/')
                idString = idString.substring(0, idString.length() - 1);
            update(item, idString.split("/"), relevance, 0);
        }

        //find and insert methods combined, used by WebCrawler
        private void update(Indexable item, String[] idSplit, int relevance, int depth) {
            if (idSplit.length == depth) {
                this.item = item;
                this.relevance += relevance;
            } else {
                ItemTree child = children.get(idSplit[depth]);
                if (child != null) {
                    child.update(item, idSplit, relevance, depth + 1);
                } else {
                    ItemTree newChild = new ItemTree(idSplit[depth], idSplit.length - 1 == depth);
                    children.put(idSplit[depth], newChild);
                    newChild.update(item, idSplit, relevance, depth + 1);
                }
            }
        }

        int find(Indexable item) {
            String itemString = item.toString();
            //chop off ending slash if necessary
            if (itemString.charAt(itemString.length() - 1) == '/')
                itemString = itemString.substring(0, itemString.length() - 1);
            return find(itemString.split("/"), 0);
        }

        private int find(String[] idSplit, int depth) {
            if (idSplit.length == depth)
                return relevance;
            ItemTree child = children.get(idSplit[depth]);
            if (child == null)
                return -1;
            else return child.find(idSplit, depth + 1);
        }

        //called by query processor
        HashSet<ItemNode> getAll() {
            HashSet<ItemNode> nodes = new HashSet<>();
            getAll(nodes);
            return nodes;
        }

        private HashSet<ItemNode> getAll(HashSet<ItemNode> temp) {
//            if (isItem)
  //              temp.add(new ItemNode(item, relevance));
            for (Map.Entry<String, ItemTree> entry : children.entrySet()) {
                entry.getValue().getAll(temp);
            }
            return temp;
        }
    }
}
