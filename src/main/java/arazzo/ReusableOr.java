package arazzo;

import java.util.Objects;

/**
 * Strongly typed generic union wrapper holding either an inline Arazzo object
 * {@code T} (such as {@link ArazzoParameter}, {@link SuccessAction}, or
 * {@link FailureAction}) or a {@link ReusableObject} referencing
 * {@code $components.*}.
 *
 * @param <T>
 *            The concrete Arazzo model type when not a reusable reference.
 */
public final class ReusableOr<T> {
	/**
	 * The inline item, or {@code null} if this instance represents a
	 * {@link ReusableObject}.
	 */
	public final T item;

	/**
	 * The reusable reference object, or {@code null} if this instance holds an
	 * inline item.
	 */
	public final ReusableObject reusable;

	/**
	 * Constructs a {@code ReusableOr} with the given inline item and reusable
	 * reference.
	 *
	 * @param item
	 *            The inline item, or {@code null}.
	 * @param reusable
	 *            The reusable reference, or {@code null}.
	 */
	public ReusableOr(T item, ReusableObject reusable) {
		this.item = item;
		this.reusable = reusable;
	}

	/**
	 * Creates a {@code ReusableOr} wrapping an inline item.
	 *
	 * @param <T>
	 *            The item type.
	 * @param item
	 *            The inline item.
	 * @return A new {@code ReusableOr} holding {@code item}.
	 */
	public static <T> ReusableOr<T> ofItem(T item) {
		return new ReusableOr<>(item, null);
	}

	/**
	 * Creates a {@code ReusableOr} wrapping a {@link ReusableObject}.
	 *
	 * @param <T>
	 *            The target item type.
	 * @param reusable
	 *            The {@link ReusableObject} instance.
	 * @return A new {@code ReusableOr} holding {@code reusable}.
	 */
	public static <T> ReusableOr<T> ofReusable(ReusableObject reusable) {
		return new ReusableOr<>(null, reusable);
	}

	/**
	 * Creates a {@code ReusableOr} wrapping a {@code $components.*} reference
	 * expression string.
	 *
	 * @param <T>
	 *            The target item type.
	 * @param reference
	 *            The {@code $components.*} runtime expression.
	 * @return A new {@code ReusableOr} holding a {@link ReusableObject} with
	 *         {@code reference}.
	 */
	public static <T> ReusableOr<T> ofReference(String reference) {
		ReusableObject r = new ReusableObject();
		r.reference = reference;
		return new ReusableOr<>(null, r);
	}

	/**
	 * Returns {@code true} if this wrapper holds a {@link ReusableObject}.
	 *
	 * @return {@code true} if {@link #reusable} is non-null.
	 */
	public boolean isReusable() {
		return reusable != null;
	}

	/**
	 * Returns {@code true} if this wrapper holds an inline item.
	 *
	 * @return {@code true} if {@link #item} is non-null.
	 */
	public boolean isItem() {
		return item != null;
	}

	/**
	 * Returns the inline item, or {@code null} if reusable.
	 *
	 * @return The inline item.
	 */
	public T getItem() {
		return item;
	}

	/**
	 * Returns the {@link ReusableObject}, or {@code null} if inline.
	 *
	 * @return The reusable object.
	 */
	public ReusableObject getReusable() {
		return reusable;
	}

	/**
	 * Compares this wrapper to another object for structural equality.
	 *
	 * @param o
	 *            The object to compare against.
	 * @return {@code true} if equal.
	 */
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof ReusableOr)) {
			return false;
		}
		ReusableOr<?> that = (ReusableOr<?>) o;
		return Objects.equals(item, that.item) && Objects.equals(reusable, that.reusable);
	}

	/**
	 * Computes the hash code for this wrapper.
	 *
	 * @return The hash code value.
	 */
	@Override
	public int hashCode() {
		return Objects.hash(item, reusable);
	}

	/**
	 * Returns a human-readable string representation of this wrapper.
	 *
	 * @return The string representation.
	 */
	@Override
	public String toString() {
		return isReusable() ? "Reusable(" + reusable.reference + ")" : "Item(" + item + ")";
	}
}
