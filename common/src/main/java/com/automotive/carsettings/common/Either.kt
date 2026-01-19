package com.automotive.carsettings.common

/**
 * Represents a value of one of two possible types (a disjoint union).
 * An Either is either a Left or a Right.
 * By convention, Left is used for failure and Right is used for success.
 */
sealed class Either<out L, out R> {
    data class Left<out L>(val value: L) : Either<L, Nothing>()
    data class Right<out R>(val value: R) : Either<Nothing, R>()

    val isLeft: Boolean
        get() = this is Left

    val isRight: Boolean
        get() = this is Right

    fun leftOrNull(): L? = when (this) {
        is Left -> value
        is Right -> null
    }

    fun rightOrNull(): R? = when (this) {
        is Left -> null
        is Right -> value
    }

    inline fun <T> fold(ifLeft: (L) -> T, ifRight: (R) -> T): T {
        return when (this) {
            is Left -> ifLeft(value)
            is Right -> ifRight(value)
        }
    }

    inline fun <T> map(transform: (R) -> T): Either<L, T> {
        return when (this) {
            is Left -> this
            is Right -> Right(transform(value))
        }
    }

    inline fun <T> mapLeft(transform: (L) -> T): Either<T, R> {
        return when (this) {
            is Left -> Left(transform(value))
            is Right -> this
        }
    }

    inline fun onLeft(action: (L) -> Unit): Either<L, R> {
        if (this is Left) {
            action(value)
        }
        return this
    }

    inline fun onRight(action: (R) -> Unit): Either<L, R> {
        if (this is Right) {
            action(value)
        }
        return this
    }
}
