(import 'com.sun.tools.attach.VirtualMachine)
(import 'java.lang.management.ManagementFactory)
(require '[clojure.java.io :as io])

(defn jvm-pid
  "Get the PID of the current JVM. Not portable. Use with caution."
  []
  (-> (.. ManagementFactory getRuntimeMXBean getName)
      (clojure.string/split #"@")
      first))

(defn jar-path [jar-name]
  (.getPath (io/resource jar-name)))

(defonce bootjar-path (jar-path "btrace-boot.jar"))
(defonce agentjar-path (jar-path "btrace-agent.jar"))
(defonce clientjar-path (jar-path "btrace-client.jar"))
(defonce toolsjar-path (clojure.string/join java.io.File/separator
                          [(System/getProperty "java.home") ".." "lib" "tools.jar"]))
;; for testing purpose
(defonce agent-args "port=3030,debug=true,unsafe=true,trackRetransforms=true,bootClassPath=/Users/marcoy/.m2/repository/com/sun/tools/btrace/btrace-boot/1.2.5.1/btrace-boot-1.2.5.1.jar,systemClassP$
th=/Users/marcoy/.jenv/versions/oracle64-1.8.0.25/lib/tools.jar,probeDescPath=.")

(defn attach-jvm [^String pid]
  (VirtualMachine/attach pid))

(defn attach-current-jvm []
  (attach-jvm (jvm-pid)))

(defn load-agent
  "Load btrace agent. TODO: Need to move agent args as parameters"
  []
  (let [vm (attach-current-jvm)]
    (.loadAgent vm agentjar-path agent-args)))
