package dev.programadorthi.routing.voyager.history

/**
 * Options that how the web history is controlled
 *
 * These options affects web application only. Memory will be used by default in other targets
 */
public enum class VoyagerHistoryMode {
    /**
     * Hash URLs pattern. E.g: host/#/path
     * Each route will have an entry on the browser history.
     * To avoid browser history, set neglect = true before routing to a route
     */
    Hash,

    /**
     * Traditional URLs pattern. E.g: host/path
     * Each route will have an entry on the browser history.
     * To avoid browser history, set neglect = true before routing to a route
     */
    Html5,

    /**
     * No updates to URL or History stack.
     * All route will be neglected.
     */
    Memory,
}
