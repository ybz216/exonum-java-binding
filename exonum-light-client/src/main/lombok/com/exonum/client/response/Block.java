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

package com.exonum.client.response;

import com.exonum.binding.common.hash.HashCode;
import com.google.gson.annotations.SerializedName;
import java.time.ZonedDateTime;
import java.util.Optional;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

/**
 * Exonum block header data structure.
 *
 * <p>A block is essentially a list of transactions, which is a result of the consensus algorithm
 * (thus authenticated by the supermajority of validators) and is applied atomically to the
 * blockchain state.
 *
 * <p>This structure only contains the amount of transactions and the transactions root hash as well
 * as other information, but not the transactions themselves.
 *
 * <p>The JSON representation of this class is compatible with the format used by Exonum.
 */
@Value
@Builder
public class Block {

  /**
   * Identifier of the leader node which has proposed the block.
   */
  @SerializedName("proposer_id")
  int proposerId;

  /**
   * The height of this block which is a distance between the last block and the "genesis"
   * block. Genesis block has 0 height. Therefore, the blockchain height is equal to
   * the number of blocks plus one.
   *
   * <p>The height also identifies each block in the blockchain.
   */
  @SerializedName("height")
  long height;

  /**
   * Number of transactions in this block.
   */
  @SerializedName("tx_count")
  int numTransactions;

  /**
   * Hash link to the previous block in the blockchain.
   */
  @NonNull
  @SerializedName("prev_hash")
  HashCode previousBlockHash;

  /**
   * Root hash of the Merkle tree of transactions in this block.
   */
  @NonNull
  @SerializedName("tx_hash")
  HashCode txRootHash;

  /**
   * Hash of the blockchain state after applying transactions in the block.
   */
  @NonNull
  @SerializedName("state_hash")
  HashCode stateHash;

  /**
   * Time when the block was committed to the blockchain.
   */
  @SerializedName("time")
  ZonedDateTime commitTime;

  /**
   * Returns the time when the block was committed to the blockchain.
   * The time is equal to the median time of submission of precommit messages confirming
   * this block by the validators.
   *
   * <p>Can be empty if include time parameter is not specified in the request.
   */
  public Optional<ZonedDateTime> getCommitTime() {
    return Optional.ofNullable(commitTime);
  }

  /**
   * Returns true if this block is empty:
   * contains no {@linkplain #getNumTransactions() transactions}.
   */
  public final boolean isEmpty() {
    return getNumTransactions() == 0;
  }
}
