; (import 'com.sun.btrace.client.Client)
; (import 'com.sun.btrace.CommandListener)
; (import 'com.sun.btrace.comm.DataCommand)
; (import 'com.sun.btrace.comm.Command)
(import 'java.lang.management.ManagementFactory)
(import 'java.io.File)
(import 'utils.Creator)

(defonce btrace-port 3030)
(defonce classpath (System/getProperty "java.class.path"))

; (defn create-cmd-listener []
;   (proxy [CommandListener] []
;     (onCommand [^Command cmd])))

(defn jvm-pid
  "Get the PID of the current JVM. Not portable. Use with caution."
  []
  (-> (.. ManagementFactory getRuntimeMXBean getName)
      (clojure.string/split #"@")
      first))

(defn bt-agentjar-path
  "Path to btrace-agent.jar"
  []
  (first (filter #(re-matches #".*btrace-agent.*" %)
                 (clojure.string/split classpath #":"))))

(defn bt-bootjar-path
  "Path to btrace-boot.jar"
  []
  (first (filter #(re-matches #".*btrace-boot.*" %)
                 (clojure.string/split classpath #":"))))

(defn bt-clientjar-path
  "Path to btrace-client.jar"
  []
  (first (filter #(re-matches #".*btrace-client.*" %)
                 (clojure.string/split classpath #":"))))

(defn attach-jvm [^String pid]
  (let [agentjar-path (bt-agentjar-path)
        bootjar-path (bt-bootjar-path)
        c (Creator/createClient btrace-port)
        ; c (Client. btrace-port "." true true true false nil)
        ]
    (System/setProperty "com.sun.btrace.debug" "true")
    (.attach c pid agentjar-path nil bootjar-path)
    ; (.attach c pid agentjar-path nil nil)
    c))

(defn submit-code [c filename]
  (let [code (.compile c filename nil)]
    (.submit c code (into-array String []) (Creator/createCmdListener))
    c))

(defn testmemory []
  (let [c (attach-jvm (jvm-pid))]
    (submit-code c "Memory.java")))
