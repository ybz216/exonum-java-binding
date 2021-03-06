// Copyright 2018 The Exonum Team
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

use integration_tests::vm::create_vm_for_tests_with_classes;
use java_bindings::utils::jni_cache;

use std::{
    sync::{Arc, Barrier},
    thread::spawn,
};

#[test]
// NOTE: This test is not supposed to reliably catch synchronization errors.
fn concurrent_cache_read() {
    const THREAD_NUM: usize = 8;
    let mut threads = Vec::new();

    // Create a VM, initializing the JNI cache
    let _jvm = create_vm_for_tests_with_classes();

    let barrier = Arc::new(Barrier::new(THREAD_NUM));

    for _ in 0..THREAD_NUM {
        let barrier = Arc::clone(&barrier);
        let jh = spawn(move || {
            barrier.wait();
            jni_cache::runtime_adapter::initialize_id();
            jni_cache::runtime_adapter::deploy_artifact_id();
            jni_cache::runtime_adapter::is_artifact_deployed_id();
            jni_cache::runtime_adapter::initiate_adding_service_id();
            jni_cache::runtime_adapter::update_service_status_id();
            jni_cache::runtime_adapter::execute_tx_id();
            jni_cache::runtime_adapter::after_transactions_id();
            jni_cache::runtime_adapter::after_commit_id();
            jni_cache::runtime_adapter::shutdown_id();
            jni_cache::class::get_name_id();
            jni_cache::object::get_class_id();
            jni_cache::classes_refs::java_lang_error();
            jni_cache::classes_refs::execution_exception();
        });
        threads.push(jh);
    }
    for jh in threads {
        jh.join().unwrap();
    }
}
