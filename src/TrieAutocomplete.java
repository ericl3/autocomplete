import java.util.*;

/**
 * General trie/priority queue algorithm for implementing Autocompletor
 * 
 * @author Austin Lu
 * @author Jeff Forbes
 */
public class TrieAutocomplete implements Autocompletor {

	/**
	 * Root of entire trie
	 */
	protected Node myRoot;

	/**
	 * Constructor method for TrieAutocomplete. Should initialize the trie rooted at
	 * myRoot, as well as add all nodes necessary to represent the words in terms.
	 * 
	 * @param terms
	 *            - The words we will autocomplete from
	 * @param weights
	 *            - Their weights, such that terms[i] has weight weights[i].
	 * @throws NullPointerException
	 *             if either argument is null
	 * @throws IllegalArgumentException
	 *             if terms and weights are different length
	 */
	public TrieAutocomplete(String[] terms, double[] weights) {
		if (terms == null || weights == null) {
			throw new NullPointerException("One or more arguments null");
		}

		if (terms.length != weights.length) {
			throw new IllegalArgumentException("terms and weights are not the same length");
		}

		Term[] myTerms = new Term[terms.length];

		HashSet<String> words = new HashSet<String>();

		for (int i = 0; i < terms.length; i++) {
			words.add(terms[i]);
			myTerms[i] = new Term(terms[i], weights[i]);
			if (weights[i] < 0) {
				throw new IllegalArgumentException("Negative weight " + weights[i]);
			}
		}
		if (words.size() != terms.length) {
			throw new IllegalArgumentException("Duplicate input terms");
		}

		// Represent the root as a dummy/placeholder node
		myRoot = new Node('-', null, 0);

		for (int i = 0; i < terms.length; i++) {
			add(terms[i], weights[i]);
		}
	}

	/**
	 * Add the word with given weight to the trie. If word already exists in the
	 * trie, no new nodes should be created, but the weight of word should be
	 * updated.
	 * 
	 * In adding a word, this method should do the following: Create any necessary
	 * intermediate nodes if they do not exist. Update the subtreeMaxWeight of all
	 * nodes in the path from root to the node representing word. Set the value of
	 * myWord, myWeight, isWord, and mySubtreeMaxWeight of the node corresponding to
	 * the added word to the correct values
	 * 
	 * @throws a
	 *             NullPointerException if word is null
	 * @throws an
	 *             IllegalArgumentException if weight is negative.
	 */
	private void add(String word, double weight) {
		// TODO: Implement add
		if (word == null) {
			throw new NullPointerException("null word");
		}
		if (weight < 0) {
			throw new IllegalArgumentException("negative weight");
		}
		Node current = myRoot;
		for (int k = 0; k < word.length(); k++) {
			char ch = word.charAt(k);
			if (current.children.get(ch) == null) {
				current.children.put(ch, new Node(ch, current, weight));
			}
			if (current.mySubtreeMaxWeight < weight) {
				current.mySubtreeMaxWeight = weight;
			}
			current = current.children.get(ch);
		}
		current.myWeight = weight;
		current.myWord = word;
		current.isWord = true;

	}

	/**
	 * Required by the Autocompletor interface. Returns an array containing the k
	 * words in the trie with the largest weight which match the given prefix, in
	 * descending weight order. If less than k words exist matching the given prefix
	 * (including if no words exist), then the array instead contains all those
	 * words. e.g. If terms is {air:3, bat:2, bell:4, boy:1}, then topKMatches("b",
	 * 2) should return {"bell", "bat"}, but topKMatches("a", 2) should return
	 * {"air"}
	 * 
	 * @param prefix
	 *            - A prefix which all returned words must start with
	 * @param k
	 *            - The (maximum) number of words to be returned
	 * @return An Iterable of the k words with the largest weights among all words
	 *         starting with prefix, in descending weight order. If less than k such
	 *         words exist, return all those words. If no such words exist, return
	 *         an empty Iterable
	 * @throws a
	 *             NullPointerException if prefix is null
	 */
	public Iterable<String> topMatches(String prefix, int k) {
		// TODO: Implement topKMatches
		if (prefix == null) {
			throw new NullPointerException("null prefix");
		}
		if (k == 0) {
			return new LinkedList<String>();
		}
		Node current = myRoot;
		for (int j = 0; j < prefix.length(); j++) {
			if (current.children.get(prefix.charAt(j)) == null) {
				return new LinkedList<String>();
			}
			current = current.children.get(prefix.charAt(j));
		}
		PriorityQueue<Node> nodePQ = new PriorityQueue<Node>(new Node.ReverseSubtreeMaxWeightComparator());
		nodePQ.add(current);
		PriorityQueue<Term> termPQ = new PriorityQueue<Term>(k, new Term.WeightOrder());
		while (nodePQ.size() > 0) {
			current = nodePQ.remove();
			if (current.isWord) {
				termPQ.add(new Term(current.getWord(), current.getWeight()));
			}
			if (termPQ.size() > k) {
				termPQ.remove();
			}
			for (Node n : current.children.values()) {
				nodePQ.add(n);
			}
			if (termPQ.peek() != null && nodePQ.peek() != null) {
				if (termPQ.peek().getWeight() > nodePQ.peek().mySubtreeMaxWeight && termPQ.size() == k) {
					break;
				}
			}
		}
		LinkedList<String> ret = new LinkedList<String>();
		while (termPQ.size() > 0) {
			ret.addFirst(termPQ.remove().getWord());
		}
		return ret;
	}

	/**
	 * Given a prefix, returns the largest-weight word in the trie starting with
	 * that prefix.
	 * 
	 * @param prefix
	 *            - the prefix the returned word should start with
	 * @return The word from with the largest weight starting with prefix, or an
	 *         empty string if none exists
	 * @throws a
	 *             NullPointerException if the prefix is null
	 */
	public String topMatch(String prefix) {
		// TODO: Implement topMatch
		if (prefix == null) {
			throw new NullPointerException("null prefix");
		}
		String theMatch = "";
		Node current = myRoot;
		for (int k = 0; k < prefix.length(); k++) {
			if (current.children.get(prefix.charAt(k)) == null) {
				return theMatch;
			}
			current = current.children.get(prefix.charAt(k));
		}
		String charLooper = "whatever"; // new character to feed
		while (current.isWord == false) { // loops until current is a word
			for (char ch : current.children.keySet()) {
				if (current.children.get(ch).mySubtreeMaxWeight == current.mySubtreeMaxWeight) {
					charLooper = current.children.get(ch).myInfo; // ensures going down max path
				}
			}
			current = current.getChild(charLooper.charAt(0));
		}
		theMatch = current.myWord;
		return theMatch;
	}

	/**
	 * Return the weight of a given term. If term is not in the dictionary, return
	 * 0.0
	 */
	public double weightOf(String term) {
		// TODO complete weightOf
		double weight = 0.0;
		Node current = myRoot;
		for (int j = 0; j < term.length(); j++) {
			if (current.children.get(term.charAt(j)) == null) {
				return weight;
			}
			current = current.children.get(term.charAt(j));
		}
		weight = current.myWeight;
		return weight;
	}
}
