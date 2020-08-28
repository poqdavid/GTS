/*
 * This file is part of LuckPerms, licensed under the MIT License.
 *
 *  Copyright (c) lucko (Luck) <luck@lucko.me>
 *  Copyright (c) contributors
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */

package me.nickimpact.gts.common.storage;

import me.nickimpact.gts.api.listings.Listing;
import me.nickimpact.gts.api.listings.SoldListing;
import me.nickimpact.gts.api.messaging.message.type.auctions.AuctionMessage;
import me.nickimpact.gts.api.messaging.message.type.listings.BuyItNowMessage;
import me.nickimpact.gts.api.player.PlayerSettings;
import me.nickimpact.gts.api.stashes.Stash;
import me.nickimpact.gts.common.plugin.GTSPlugin;
import me.nickimpact.gts.api.storage.GTSStorage;
import me.nickimpact.gts.api.util.ThrowingRunnable;
import me.nickimpact.gts.common.storage.implementation.StorageImplementation;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Predicate;

public class GTSStorageImpl implements GTSStorage {

    private final GTSPlugin plugin;
    private final StorageImplementation implementation;

    public GTSStorageImpl(GTSPlugin plugin, StorageImplementation implementation) {
        this.plugin = plugin;
        this.implementation = implementation;
    }

    /**
     * Attempts to initialize the storage implementation
     */
    public void init() {
        try {
            this.implementation.init();
        } catch (Exception e) {
            // Log the failure
            e.printStackTrace();
        }
    }

    /**
     * Attempts to shutdown the storage implementation
     */
    public void shutdown() {
        try {
            this.implementation.shutdown();
        } catch (Exception e) {
            // Log the failure
            e.printStackTrace();
        }
    }

    /**
     * Represents any properties which might be set against a storage
     * implementation.
     *
     * @return A mapping of flags to values representing storage implementation
     * properties
     */
    public Map<String, String> getMeta() {
        return this.implementation.getMeta();
    }

    @Override
    public CompletableFuture<Boolean> publishListing(Listing listing) {
        return null;
    }

    private <T> CompletableFuture<T> makeFuture(Callable<T> supplier) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return supplier.call();
            } catch (Exception e) {
                if (e instanceof RuntimeException) {
                    throw (RuntimeException) e;
                }
                throw new CompletionException(e);
            }
        });
    }

    private CompletableFuture<Void> makeFuture(ThrowingRunnable runnable) {
        return CompletableFuture.runAsync(() -> {
            try {
                runnable.run();
            } catch (Exception e) {
                if (e instanceof RuntimeException) {
                    throw (RuntimeException) e;
                }
                throw new CompletionException(e);
            }
        });
    }

    @Override
    public CompletableFuture<Boolean> purge() {
        return this.makeFuture(this.implementation::purge);
    }

    @Override
    public CompletableFuture<Optional<PlayerSettings>> getPlayerSettings(UUID uuid) {
        return null;
    }

    @Override
    public CompletableFuture<Boolean> applyPlayerSettings(UUID uuid, PlayerSettings settings) {
        return null;
    }

    @Override
    public CompletableFuture<AuctionMessage.Bid.Response> processBid(AuctionMessage.Bid.Request request) {
        return this.makeFuture(() -> this.implementation.processBid(request));
    }

    @Override
    public CompletableFuture<BuyItNowMessage.Remove.Response> processListingRemoveRequest(BuyItNowMessage.Remove.Request request) {
        return null;
    }

    @Override
    public CompletableFuture<List<Listing>> fetchListings(Collection<Predicate<Listing>> filters) {
        return null;
    }

    @Override
    public CompletableFuture<Stash<?, ?>> fetchStash(UUID user) {
        return null;
    }
}