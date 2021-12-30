//todo-delete import- added to make heapPrint work
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * FibonacciHeap
 *
 * An implementation of a Fibonacci Heap over integers.
 */
public class FibonacciHeap
{
    private HeapNode minNode;
    private HeapNode head; // circular array
    private HeapNode tail; // circular array
    private int size;
    public static int totalCuts = 0;
    public static int totalLinks = 0;
    public int markedNodesCnt = 0;
    public int treesCnt = 0;
    //todo-delete stream- added to make heapPrint work
    static final PrintStream stream = System.out;

    public FibonacciHeap(){
        // todo-think about constructors
        this.size=0;
        this.minNode =null;
        this.head =null;
        this.tail =null;
        // todo-potential is 0 when creating a new FibHeap
//        markedNodesCnt = 0 and treesCnt = 0, that's what defines the potential so it's 0
    }
    /**
     * public boolean isEmpty()
     *
     * Returns true if and only if the heap is empty.
     *
     */
    public boolean isEmpty()
    {
        return (this.size == 0);
    }

    /**
     * public HeapNode insert(int key)
     *
     * Creates a node (of type HeapNode) which contains the given key, and inserts it into the heap.
     * The added key is assumed not to already belong to the heap.
     *
     * Returns the newly created node.
     */
    public HeapNode insert(int key)
    {
        HeapNode nodeToInsert;
        if (isEmpty()){
            nodeToInsert = new HeapNode(key); // create a B_0 tree with key
            // TODO: 21/12/2021 check if needs to make a method
            nodeToInsert.setNext(nodeToInsert);
            nodeToInsert.setPrev(nodeToInsert);
            this.head = nodeToInsert;
            this.tail = nodeToInsert;
            this.minNode = nodeToInsert;
        }else{
            nodeToInsert = new HeapNode(key, this.tail, this.head);
//            update prev and next of current head and tail
            this.head.setPrev(nodeToInsert);
            this.tail.setNext(nodeToInsert);
            this.head = nodeToInsert; // create a B_0 tree, parent is null, no child and next is head and prev is tail
            if (nodeToInsert.getKey() < this.minNode.getKey()){
                this.minNode = nodeToInsert; // update minNode if needed
            }
        }
        this.size++; //added a node
        this.treesCnt++; // added a tree
        return nodeToInsert;
    }

    /**
     * public void deleteMin()
     *
     * Deletes the node containing the minimum key.
     *
     */
    public void deleteMin() {
        HeapNode minHeapNode=this.minNode;
        if (minHeapNode!= null) {
            updateRoots(minHeapNode);
            removeMin(minHeapNode); //size is updated in remove min
            if (this.size()>0){ // if after removeHeap the heap still has nodes then consolidation is needed.
                //print(this,true);
                consolidation();
            }
        }
    }

    private void removeMin(HeapNode minNode) {
        if (!this.isEmpty()) {
            //at least one heapNode
            if (this.size() == 1) {
                //only minNode in heap
                initializeFields();
            } else { //size>1
                if (minNode.getRank() == 0) { //there is more than 0 node in the heap minNode doesn't have children
                    //no need to take care of pointers of its children
                    removeHeapNode(this.minNode);
                    //print(this,true);

                } else {
                    //minNode has at least one Child
                    if (this.minNode == this.head) { //minNode is the head of the root List
                        HeapNode lastChild=this.minNode.getChild().getPrev(); //todo- what happen when only one child?
                        if(minNode.getNext()==minNode){ // min node is the only tree in the heap
                            this.head=this.minNode.getChild();
                            lastChild.setNext(this.head);
                            this.head.setPrev(lastChild);
                            this.tail.setNext(this.head);
                        }
                        else{
                            //circular list so last child is th prev to child
                            this.head=this.minNode.getChild();
                            lastChild.setNext(this.minNode.getNext());
                            this.minNode.getNext().setPrev(lastChild);
                            this.head.setPrev(this.tail);
                            this.tail.setNext(this.head);
                        }



                    } else if (this.minNode == this.tail) { //minNode is the tail of the root List
                        HeapNode prevHN=this.minNode.getPrev();
                        HeapNode lastChild=this.minNode.getChild().getPrev(); //circular list so last child is th prev to child
                        prevHN.setNext(this.minNode.getChild());
                        this.minNode.getChild().setPrev(prevHN);
                        this.head.setPrev(lastChild);

                    } else { // this min node is in between && has at least one child
                        this.minNode.getChild().getPrev().setNext(this.minNode.getNext()); // x_k -> y_3
                        this.minNode.getNext().setPrev(this.minNode.getChild().getPrev()); // y_3 -> x_k
                        this.minNode.getChild().setPrev(this.minNode.getPrev()); // x_1 -> y_1
                        this.minNode.getPrev().setNext(this.minNode.getChild()); // y_1 -> x_1


                    }

                }
                this.size--;
            }

        }

    }

    private void consolidation() {
        // successive linking- creates a valid Binomial Heap from FibHeap

        HeapNode [] rankArray = new HeapNode[(int)(Math.log(this.size) / Math.log(2)) + 1]; // there are log_2(n) trees at most
//        for (int i=0;i<rankArray.length;i++){ //todo- its not harmful-this is should stay- check after all is good if nessecerry
//            rankArray[i]=null;
//        }
        HeapNode currRoot = this.head;
        HeapNode nextRoot= currRoot.getNext();
        int cntIteration=0;
        int currRank=0;
        if (rankArray[currRoot.getRank()]==null){
            rankArray[currRoot.getRank()]=currRoot;
        }
        currRoot=currRoot.getNext();
        nextRoot=currRoot.getNext();
        cntIteration++;

        int treeCounterAtFirst=this.treesCnt;
        while (currRoot!=null && currRoot!=this.head && cntIteration < treeCounterAtFirst){
            HeapNode xRoot=currRoot;
            currRank=xRoot.getRank();
            nextRoot=currRoot.getNext();

            // empty box
            if (rankArray[currRank]==null){ //there is no root with the same rank
                rankArray[currRank]=xRoot;
//                print(this,true);
            }
            else { // there is a tree with the same rank as currRoot
                // we should link them
                while (currRank<rankArray.length && rankArray[currRank]!=null){
                    HeapNode sameRankTree=rankArray[currRank];
                    xRoot=link(xRoot,sameRankTree);
                    rankArray[currRank]=null; //after linking two trees with rank r ==> currently  no tree in rank r.
                    currRank++;
                }
                rankArray[xRoot.getRank()]=xRoot; // linking is done
            }
            currRoot=nextRoot;
            cntIteration++;
        }

        // now we should make sure the roots "list" is ordered by increasing rank
        //so we will collect trees with lower rank first and insert after them

        //initializeFields();//including minNode=null;//todo-check if necessary
        this.minNode=null;
        this.treesCnt=0;//check that
        HeapNode pos=null; //pointer to currTree
        for(int j=0;j<rankArray.length;j++){
            if(rankArray[j]!=null){
                if (this.minNode==null){
                    //the heap is empty
                    pos=rankArray[j];
                    //HeapNode firstTree=rankArray[j];
                    pos.setNext(pos);
                    pos.setPrev(pos);
                    this.tail=pos;
                    this.head =pos;
                    this.minNode=pos;
                }
                else{
                    //roots list isnt empty-we should update pointers
                    // "inserting" currTree into the list of roots.
                    //for each nonempty index theres a tree with its rank so rootsNum
                    // so treesCnt should get increased by 1
                    insertAfter(pos,rankArray[j]);
                    //rankArray[j] becomes a root so is mark=false;
                    rankArray[j].setMarked(false);
                    this.tail=rankArray[j];//tail is updated though out the loop
                    //this.size+=rankArray[j].getRank()+1;
                    //maintain the minNode
                    if(rankArray[j].getKey() < this.minNode.getKey()&& rankArray[j].getKey()< pos.getKey()){
                        this.minNode=rankArray[j];
                        pos=rankArray[j];
                    }
                    else {
                        pos=pos.getNext();
                    }
                }
                this.treesCnt+=1;//inserted new tree to heap
            }
        }
        //maintain pointers of circular list
        this.head.setPrev(this.tail);
        this.tail.setNext(this.head);

    }

    private void initializeFields() {
        //sets all fields to their default value
        this.minNode =null;
        this.treesCnt=0;
        this.size=0;
        this.markedNodesCnt=0;
        this.head=null;
        this.tail=null;
    }
    private void insertAfter(HeapNode currNode, HeapNode nextNode) {
        nextNode.setPrev(currNode);
        nextNode.setNext(currNode.getNext());
        currNode.setNext(nextNode);
        nextNode.getNext().setPrev(nextNode);
    }

    private HeapNode link(HeapNode currRoot, HeapNode rootToLink) {
        //link bigger to smaller
        //remove heapNode from rootsList
        if (currRoot.getKey() < rootToLink.getKey()){ // currRoot is smaller
            removeHeapNode(rootToLink);// treeCnt is updated inside
            insertHeapNodeAsFirst(currRoot,rootToLink);
            //link done
            totalLinks++;
            return currRoot;

        }else { // currRoot is bigger
            removeHeapNode(currRoot); // treeCnt is updated inside
            insertHeapNodeAsFirst(rootToLink,currRoot);
            //link done
            totalLinks++;
            return rootToLink;
        }
    }


    private void insertHeapNodeAsFirst(HeapNode smaller, HeapNode bigger) {

        if (smaller.getRank()==0){
            //smaller has no children- B_0
            bigger.setNext(bigger);
            bigger.setPrev(bigger);
            smaller.setChild(bigger);
            bigger.setParent(smaller);
        }
        else {//smaller has at least one child
            HeapNode last=smaller.getChild().getPrev();
            HeapNode second=smaller.getChild();
            bigger.setNext(second);
            second.setPrev(bigger);
            bigger.setPrev(last);
            last.setNext(bigger);
            bigger.setParent(smaller);
            smaller.setChild(bigger);

        }
        smaller.setRank(smaller.getRank()+1);
        bigger.setMarked(false);
    }

    private void removeHeapNode(HeapNode bigger) {
        //this method removes a given heap node from the list of roots
        // @pre: bigger is in the heap
        if (bigger==this.head){
            //bigger is the head of rootsList
            this.head=this.head.getNext();
            this.tail.setNext(this.head);
            this.head.setPrev(this.tail);
            //print(this,true);
        }
        else{
            //bigger is not the head
            HeapNode prevHN=bigger.getPrev();
            prevHN.setNext(prevHN.getNext().getNext());
            if (prevHN.getNext()!=null){
                prevHN.getNext().setPrev(prevHN);
            }
            if (bigger==this.tail){
                //bigger is the last tree in rootsList
                this.tail=prevHN;
//                print(this,true);
            }
        }
        this.treesCnt-=1;

    }

    private void updateRoots(HeapNode minHeapNode){
        if (minHeapNode!=null){
            if (minHeapNode.getRank() > 0){
                HeapNode currChild=this.minNode.getChild();
                currChild.getPrev().setNext(null);//cancel the circularity in children list//todo-check that
                while (currChild!=null){
                    currChild.setParent(null);
                    currChild.setMarked(false); // becomes a root - root is never marked
                    this.treesCnt+=1;
                    currChild=currChild.getNext();
                }
            }

        }

    }

    /**
     * public HeapNode findMin()
     *
     * Returns the node of the heap whose key is minimal, or null if the heap is empty.
     *
     */
    public HeapNode findMin()
    {
        if (this.isEmpty()){
            return null;
        }
        return this.minNode;
    }

    /**
     * public void meld (FibonacciHeap heap2)
     *
     * Melds heap2 with the current heap.
     *
     */
    public void meld (FibonacciHeap heap2)
    {
        if (this.isEmpty()&&!heap2.isEmpty()){
            //this heap is empty- should update rootsList to be heap2s' rootsList
            this.head=heap2.head;
            this.tail=heap2.tail;
            this.head.setPrev(this.tail);
            this.tail.setNext(this.head);
            //number of trees doesnt change-potential doesnt change

        }
        if(!this.isEmpty()&& !heap2.isEmpty()){
            //if !this.isEmpty()&& heap2.isEmpty() - nothing to do
            // next case we have to deal with- both not empty
            this.tail.setNext(heap2.head);
            heap2.head.setPrev(this.tail);
            this.tail=heap2.tail;
            this.tail.setNext(this.head);
            this.head.setPrev(this.tail);
            //maintain minHeapNode
            if((this.minNode==null) ||(heap2.minNode!=null)&& heap2.minNode.getKey()<this.minNode.getKey()){
                this.minNode=heap2.minNode;
            }
            //maintain other fields
            this.size=this.size()+ heap2.size();
            this.treesCnt+=heap2.treesCnt;
            this.markedNodesCnt+=heap2.markedNodesCnt;
            this.treesCnt+=heap2.treesCnt;
            //number of trees,markedNodesCnt might change --> potential might change
        }
    }
    /**
     * public int size()
     *
     * Returns the number of elements in the heap.
     *
     */
    public int size()
    {
        return this.size;
    }

    /**
     * public int[] countersRep()
     *
     * Return an array of counters. The i-th entry contains the number of trees of order i in the heap.
     * Note: The size of of the array depends on the maximum order of a tree, and an empty heap returns an empty array.
     *
     */
    public int[] countersRep()
    {
        if (this.isEmpty()){
            return new int[]{};
        }
        int[] counterArr = new int[treesCnt];
        HeapNode currTree=this.head;
        counterArr[currTree.getRank()]+=1;
        currTree=currTree.getNext();
        while(currTree!=this.head){
            counterArr[currTree.getRank()]+=1;
        }
        return counterArr; //	 to be replaced by student code
    }

    /**
     * public void delete(HeapNode x)
     *
     * Deletes the node x from the heap.
     * It is assumed that x indeed belongs to the heap.
     *
     */
    public void delete(HeapNode x)
    {
        // decrease-key to infinity
        // TODO: 21/12/2021 can we insert a negative key?
        x.setKey(Integer.MIN_VALUE);
        this.decreaseKey(x, 0);
        this.deleteMin();
    }

    /**
     * public void decreaseKey(HeapNode x, int delta)
     *
     * Decreases the key of the node x by a non-negative value delta. The structure of the heap should be updated
     * to reflect this change (for example, the cascading cuts procedure should be applied if needed).
     */
    public void decreaseKey(HeapNode x, int delta)
    {
        if (x.getParent() == null) { // x is a root
            x.setKey(x.getKey() - delta);
            if (x.getKey() < this.minNode.getKey()){ // no need to update min if it's not a root
                this.minNode = x;
            }
        } else if (x.getKey() - delta >= x.getParent().getKey()) { // no need to cut
            x.setKey(x.getKey() - delta);

        } else {
            x.setKey(x.getKey() - delta); // key is bigger than parent, need to cut
            cascadingCut(x, x.getParent());
        }
    }

    private void cascadingCut(HeapNode x, HeapNode parent){
        totalCuts++;
        cut(x, parent);
        if (parent.getParent() != null) { // parent not is a root
            if (!parent.isMarked()) { // this is the first cut = parent not marked
                parent.setMarked(true);
                markedNodesCnt++; // for calculating potential
            }
            else { // parent is marked
                cascadingCut(parent, parent.getParent());
            }
        }
    }

    private void cut(HeapNode x, HeapNode parent){
        x.setParent(null);
        if (x.isMarked()){
            this.markedNodesCnt--; // for potential counting
        }
        x.setMarked(false);
        parent.setRank(parent.getRank()-1); // parent lost a child
        if (parent.getRank() == 0) { // parent lost his one and lonely child
            parent.setChild(null); // no more childs left for parent
            x.changePointers(this.head, this.tail); // asuming x is the child of parent
            this.head = x;
            if (x.getKey() < this.minNode.getKey()){ // no need to update min if it's not a root
                this.minNode = x;
            }
            this.treesCnt += 1; // for potential counting
        } else if (parent.getChild() == x) {
            parent.setChild(x.getNext());
            update(x);
        } else {
            update(x);
        }
    }

    private void update(HeapNode x) {
        x.getNext().setPrev(x.getPrev());
        x.getPrev().setNext(x.getNext());
        x.changePointers(this.head, this.tail);
        this.head = x;
        if (x.getKey() < this.minNode.getKey()){ // no need to update min if it's not a root
            this.minNode = x;
        }
        this.treesCnt += 1; // for potential counting
    }


    /**
     * public int potential()
     *
     * This function returns the current potential of the heap, which is:
     * Potential = #trees + 2*#marked
     *
     * In words: The potential equals to the number of trees in the heap
     * plus twice the number of marked nodes in the heap.
     */
    public int potential()
    {
        return treesCnt+2*markedNodesCnt; // should be replaced by student code
    }

    /**
     * public static int totalLinks()
     *
     * This static function returns the total number of link operations made during the
     * run-time of the program. A link operation is the operation which gets as input two
     * trees of the same rank, and generates a tree of rank bigger by one, by hanging the
     * tree which has larger value in its root under the other tree.
     */
    public static int totalLinks()
    {
        return totalLinks;
    }

    /**
     * public static int totalCuts()
     *
     * This static function returns the total number of cut operations made during the
     * run-time of the program. A cut operation is the operation which disconnects a subtree
     * from its parent (during decreaseKey/delete methods).
     */
    public static int totalCuts()
    {
        return totalCuts; // should be replaced by student code
    }

    /**
     * public static int[] kMin(FibonacciHeap H, int k)
     *
     * This static function returns the k smallest elements in a Fibonacci heap that contains a single tree.
     * The function should run in O(k*deg(H)). (deg(H) is the degree of the only tree in H.)
     *
     * ###CRITICAL### : you are NOT allowed to change H.
     */
    public static int[] kMin(FibonacciHeap H, int k)
    {
        // implementation similar to class solution
        if (H.isEmpty() || k <= 0) {
            return new int[0];
        }
        int[] arr = new int[k];
        FibonacciHeap helpHeap = new FibonacciHeap();
        helpHeap.insert(H.findMin().getKey());
        for(int i = 0; i < k; i++) {
            arr[i] = helpHeap.minNode.getKey();
            HeapNode childInd = helpHeap.minNode.getChild();
            helpHeap.deleteMin();
//            each node has a lot of children, not just 2 unlike a binary heap
//            we'll insert the root and iterate over it's children
//            then find the min node in helpHeap again and repeat
            if (childInd != null) {
                HeapNode cur = childInd;
                helpHeap.insert(childInd.getKey());
                childInd = childInd.getNext();
                while (childInd!=cur){
                    helpHeap.insert(childInd.getKey());
                    childInd = childInd.getNext();
                }
            }
        }
        return arr;
    }
    public HeapNode getFirst() {
        return this.head;
    }
    //todo-heapPrint method
    static void printIndentPrefix(ArrayList<Boolean> hasNexts) {
        int size = hasNexts.size();
        for (int i = 0; i < size - 1; ++i) {
            stream.format("%c   ", hasNexts.get(i).booleanValue() ? '│' : ' ');
        }
    }

    static void printIndent(FibonacciHeap.HeapNode heapNode, ArrayList<Boolean> hasNexts) {
        int size = hasNexts.size();
        printIndentPrefix(hasNexts);

        stream.format("%c── %s\n",
                hasNexts.get(size - 1) ? '├' : '╰',
                heapNode == null ? "(null)" : String.valueOf(heapNode.getKey())
        );
    }

    static String repeatString(String s,int count){
        StringBuilder r = new StringBuilder();
        for (int i = 0; i < count; i++) {
            r.append(s);
        }
        return r.toString();
    }

    static void printIndentVerbose(FibonacciHeap.HeapNode heapNode, ArrayList<Boolean> hasNexts) {
        int size = hasNexts.size();
        if (heapNode == null) {
            printIndentPrefix(hasNexts);
            stream.format("%c── %s\n", hasNexts.get(size - 1) ? '├' : '╰', "(null)");
            return;
        }

        Function<Supplier<FibonacciHeap.HeapNode>, String> keyify = (f) -> {
            FibonacciHeap.HeapNode node = f.get();
            return node == null ? "(null)" : String.valueOf(node.getKey());
        };
        String title  = String.format(" Key: %d ", heapNode.getKey());
        List<String> content =  Arrays.asList(
                String.format(" Rank: %d ", heapNode.getRank()),
                String.format(" Marked: %b ", heapNode.isMarked()),
                String.format(" Parent: %s ", keyify.apply(heapNode::getParent)),
                String.format(" Next: %s ", keyify.apply(heapNode::getNext)),
                String.format(" Prev: %s ", keyify.apply(heapNode::getPrev)),
                String.format(" Child: %s", keyify.apply(heapNode::getChild))
        );

        /* Print details in box */
        int length = Math.max(
                title.length(),
                content.stream().map(String::length).max(Integer::compareTo).get()
        );
        String line = repeatString("─", length);
        String padded = String.format("%%-%ds", length);
        boolean hasNext = hasNexts.get(size - 1);

        //print header row
        printIndentPrefix(hasNexts);
        stream.format("%c── ╭%s╮%n", hasNext ? '├' : '╰', line);

        //print title row
        printIndentPrefix(hasNexts);
        stream.format("%c   │" + padded + "│%n", hasNext ? '│' : ' ', title);

        // print separator
        printIndentPrefix(hasNexts);
        stream.format("%c   ├%s┤%n", hasNext ? '│' : ' ', line);

        // print content
        for (String data : content) {
            printIndentPrefix(hasNexts);
            stream.format("%c   │" + padded + "│%n", hasNext ? '│' : ' ', data);
        }

        // print footer
        printIndentPrefix(hasNexts);
        stream.format("%c   ╰%s╯%n", hasNext ? '│' : ' ', line);
    }

    static void printHeapNode(FibonacciHeap.HeapNode heapNode, FibonacciHeap.HeapNode until, ArrayList<Boolean> hasNexts, boolean verbose) {
        if (heapNode == null || heapNode == until) {
            return;
        }
        hasNexts.set(
                hasNexts.size() - 1,
                heapNode.getNext() != null && heapNode.getNext() != heapNode && heapNode.getNext() != until
        );
        if (verbose) {
            printIndentVerbose(heapNode, hasNexts);
        } else {
            printIndent(heapNode, hasNexts);
        }

        hasNexts.add(false);
        printHeapNode(heapNode.getChild(), null, hasNexts, verbose);
        hasNexts.remove(hasNexts.size() - 1);

        until = until == null ? heapNode : until;
        printHeapNode(heapNode.getNext(), until, hasNexts, verbose);
    }

    public static void print(FibonacciHeap heap, boolean verbose) {
        if (heap == null) {
            stream.println("(null)");
            return;
        } else if (heap.isEmpty()) {
            stream.println("(empty)");
            return;
        }

        stream.println("╮");
        ArrayList<Boolean> list = new ArrayList<>();
        list.add(false);
        printHeapNode(heap.getFirst(), null, list, verbose);
    }
    //todo-heapPrint ends here-delete afterwards.

    /**
     * public class HeapNode
     *
     * If you wish to implement classes other than FibonacciHeap
     * (for example HeapNode), do it in this file, not in another file.
     *
     */
    public static class HeapNode{

        public int key;
        //Todo- add info? which type? String? is it necessary?
        private int rank;
        private boolean marked;
        private HeapNode child;//leftmost child
        private HeapNode next;
        private HeapNode prev;
        private HeapNode parent;

        public HeapNode(int key) {
            this.key = key;
        }

        public HeapNode(int key, HeapNode tail, HeapNode head) {
            this.key=key;
            this.prev=tail;
            this.next=head;
        }

        public int getKey() {
            return this.key;
        }
        public void setKey(int i) {
            this.key = i;
        }
        public int getRank() {
            return rank;
        }

        public void setRank(int rank) {
            this.rank = rank;
        }
        public boolean isMarked() {
            return marked;
        }

        public void setMarked(boolean marked) {
            this.marked = marked;
        }

        public HeapNode getChild() {
            return child;
        }

        public void setChild(HeapNode child) {
            this.child = child;
        }

        public HeapNode getNext() {
            return next;
        }

        public void setNext(HeapNode next) {
            this.next = next;
        }

        public HeapNode getPrev() {
            return prev;
        }

        public void setPrev(HeapNode prev) {
            this.prev = prev;
        }

        public HeapNode getParent() {
            return parent;
        }

        public void setParent(HeapNode parent) {
            this.parent = parent;
        }
        public void changePointers(HeapNode x, HeapNode y) {
            this.next = x;
            this.prev = y;
            x.prev = this;
            y.next = this;
        }
    }
}