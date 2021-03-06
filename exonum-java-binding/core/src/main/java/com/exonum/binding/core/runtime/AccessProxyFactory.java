/*
 * Copyright 2019 The Exonum Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.exonum.binding.core.runtime;

import com.exonum.binding.core.blockchain.BlockchainData;
import com.exonum.binding.core.proxy.Cleaner;
import com.exonum.binding.core.storage.database.Fork;
import com.exonum.binding.core.storage.database.Snapshot;

/**
 * A factory of Access proxies.
 *
 * <p>This class is thread-safe.
 */
public enum AccessProxyFactory implements AccessFactory {
  INSTANCE;

  /** Returns an instance of this factory. */
  public static AccessFactory getInstance() {
    return INSTANCE;
  }

  @Override
  public Snapshot createSnapshot(long nativeHandle, Cleaner cleaner) {
    return Snapshot.newInstance(nativeHandle, cleaner);
  }

  @Override
  public Fork createFork(long nativeHandle, Cleaner cleaner) {
    return Fork.newInstance(nativeHandle, cleaner);
  }

  @Override
  public BlockchainData createBlockchainData(long nativeHandle, Cleaner cleaner) {
    return BlockchainData.fromHandle(nativeHandle, cleaner);
  }
}
