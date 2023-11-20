(ns org.soulspace.overarch.domain.views.container-view-test
  (:require [clojure.test :refer :all]
            [org.soulspace.overarch.util.functions :as fns]
            [org.soulspace.overarch.domain.view :refer :all]
            [org.soulspace.overarch.domain.views.container-view :refer :all]
            [org.soulspace.overarch.domain.model-test :as model-test]
            [org.soulspace.overarch.domain.model :as model]))

(deftest render-element?-test
  (testing "render-element?"
    (are [x y] (= x (fns/truthy? (render-model-node? {:el :container-view} y)))
      true {:el :enterprise-boundary}
      true {:el :context-boundary}
      true {:el :person}
      true {:el :system}
      true {:el :system-boundary}
      true {:el :container}
      false {:el :container-boundary}
      false {:el :component}
      false {:el :node}
      false {:el :actor}
      false {:el :use-case}
      false {:el :uses}
      false {:el :include}
      false {:el :extends}
      false {:el :generalizes}
      false {:el :state-machine}
      false {:el :start-state}
      false {:el :state}
      false {:el :end-state}
      false {:el :transition}
      false {:el :fork}
      false {:el :join}
      false {:el :choice}
      false {:el :history-state}
      false {:el :deep-history-state}
      false {:el :package}
      false {:el :class}
      false {:el :field}
      false {:el :method}
      false {:el :interface}
      false {:el :enum}
      false {:el :inheritance}
      false {:el :implementation}
      false {:el :composition}
      false {:el :aggregation}
      false {:el :association}
      false {:el :dependency}
      false {:el :stereotype}
      false {:el :annotation}
      false {:el :namespace}
      false {:el :function}
      false {:el :protocol}
      false {:el :concept})))

(deftest as-boundary?-test
  (testing "as-boundary?"
    (are [x y] (= x (fns/truthy? (as-boundary? y)))
      true {:el :system :ct #{{:el :container}}}
      false {:el :container :ct #{{:el :component}}}
      false {:el :system}
      false {:el :container}
      false {:el :component})))

(deftest render-relation-node?-test
  (testing "render-relation-node?"
    (are [x y] (= x (fns/truthy? (render-relation-node? {:el :container-view} y)))
      true {:el :person}
      true {:el :system :external true}
      true {:el :container}
      true {:el :system :external false}
      false {:el :system :external false :ct #{{:el :container}}}
      false {:el :component})))

(def container-view1
  {:el :container-view
   :id :test/container-view1
   :title "Container View 1"
   :ct [{:ref :test/user1}
        {:ref :test/system1}
        {:ref :test/ext-system1}
        {:ref :test/user1-uses-container1}
        {:ref :test/container1-calls-ext-system1}]})

(def container-view1-related
  {:el :container-view
   :id :test/container-view1-related
   :title "Container View 1"
   :ct [{:ref :test/user1-uses-container1}
        {:ref :test/container1-calls-ext-system1}]})

(def container-view1-relations
  {:el :container-view
   :id :test/container-view1-relations
   :title "Container View 1"
   :ct [{:ref :test/user1}
        {:ref :test/system1}
        {:ref :test/ext-system1}]})

(deftest referenced-model-nodes-test
  (let [c4-1 (model/build-registry model-test/c4-model1)]
    (testing "referenced-model-nodes for container view"
      (are [x y] (= x y)
        3 (count (referenced-model-nodes c4-1 container-view1))
        0 (count (referenced-model-nodes c4-1 container-view1-related))
        3 (count (referenced-model-nodes c4-1 container-view1-relations))))))

(deftest referenced-relations-test
  (let [c4-1 (model/build-registry model-test/c4-model1)]
    (testing "referenced-relations for container view"
      (are [x y] (= x y)
        2 (count (referenced-relations c4-1 container-view1))
        2 (count (referenced-relations c4-1 container-view1-related))
        0 (count (referenced-relations c4-1 container-view1-relations))))))

(deftest referenced-elements-test
  (let [c4-1 (model/build-registry model-test/c4-model1)]
    (testing "referenced-elements for container view"
      (are [x y] (= x y)
        5 (count (referenced-elements c4-1 container-view1))
        2 (count (referenced-elements c4-1 container-view1-related))
        3 (count (referenced-elements c4-1 container-view1-relations))))))

(deftest specified-model-nodes-test
  (let [c4-1 (model/build-registry model-test/c4-model1)]
    ; TODO check
    (testing "specified-model-nodes for container view"
      (are [x y] (= x y)
        3 (count (specified-model-nodes c4-1 container-view1))
        3 (count (specified-model-nodes c4-1 container-view1-related))
        3 (count (specified-model-nodes c4-1 container-view1-relations))))))

(deftest specified-relations-test
  (let [c4-1 (model/build-registry model-test/c4-model1)]
    ; TODO check
    (testing "specified-relations for container view"
      (are [x y] (= x y)
        2 (count (specified-relations c4-1 container-view1))
        2 (count (specified-relations c4-1 container-view1-related))
        2 (count (specified-relations c4-1 container-view1-relations))))))

(deftest specified-elements-test
  (let [c4-1 (model/build-registry model-test/c4-model1)]
    ; TODO check
    (testing "specified-elements for container view"
      (are [x y] (= x y)
        5 (count (specified-elements c4-1 container-view1))
        5 (count (specified-elements c4-1 container-view1-related))
        5 (count (specified-elements c4-1 container-view1-relations))))))

(comment
  (def c4-1 (model/build-registry model-test/c4-model1))
  (referenced-model-nodes c4-1 container-view1-related)

  (elements-to-render c4-1 container-view1)
  (elements-to-render c4-1 container-view1 (:ct container-view1))
  (elements-to-render c4-1 container-view1 (:ct (model/resolve-ref c4-1 :test/system1)))

)
