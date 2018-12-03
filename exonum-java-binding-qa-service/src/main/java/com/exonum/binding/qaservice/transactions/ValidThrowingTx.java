/*
 * Copyright 2018 The Exonum Team
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

package com.exonum.binding.qaservice.transactions;

import static com.exonum.binding.qaservice.transactions.TransactionPreconditions.checkTransaction;

import com.exonum.binding.qaservice.QaSchema;
import com.exonum.binding.qaservice.transactions.TxMessageProtos.ValidThrowingTxBody;
import com.exonum.binding.transaction.RawTransaction;
import com.exonum.binding.transaction.Transaction;
import com.exonum.binding.transaction.TransactionContext;
import com.google.common.annotations.VisibleForTesting;
import com.google.protobuf.InvalidProtocolBufferException;
import java.util.Objects;

public final class ValidThrowingTx implements Transaction {

  private static final short ID = QaTransaction.VALID_THROWING.id();

  private final long seed;

  public ValidThrowingTx(long seed) {
    this.seed = seed;
  }

  /**
   * First clears the indices of the service, then throws an exception.
   *
   * @throws IllegalStateException always
   */
  @Override
  public void execute(TransactionContext context) {
    QaSchema schema = new QaSchema(context.getFork());

    // Attempt to clear all service indices.
    schema.clearAll();

    // Throw an exception. Framework must revert the changes made above.
    throw new IllegalStateException("#execute of this transaction always throws: " + this);
  }

  @Override
  public RawTransaction getRawTransaction() {
    return converter().toRawTransaction(this);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ValidThrowingTx that = (ValidThrowingTx) o;
    return seed == that.seed;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(seed);
  }

  static TransactionMessageConverter<ValidThrowingTx> converter() {
    return TransactionConverter.INSTANCE;
  }

  private enum TransactionConverter implements TransactionMessageConverter<ValidThrowingTx> {
    INSTANCE;

    @Override
    public ValidThrowingTx fromRawTransaction(RawTransaction rawTransaction) {
      checkRawTransaction(rawTransaction);

      try {
        long seed = ValidThrowingTxBody.parseFrom(rawTransaction.getPayload())
            .getSeed();
        return new ValidThrowingTx(seed);
      } catch (InvalidProtocolBufferException e) {
        throw new IllegalArgumentException(e);
      }
    }

    @Override
    public RawTransaction toRawTransaction(ValidThrowingTx transaction) {
      return transaction.getRawTransaction();
    }

    private void checkRawTransaction(RawTransaction rawTransaction) {
      checkTransaction(rawTransaction, ID);
    }
  }

  @VisibleForTesting
  static byte[] serializeBody(ValidThrowingTx transaction) {
    return ValidThrowingTxBody.newBuilder()
        .setSeed(transaction.seed)
        .build()
        .toByteArray();
  }
}
