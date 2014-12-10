(import 'com.sun.tools.attach.VirtualMachine)
(import 'java.lang.management.ManagementFactory)
(import '[java.io File PrintWriter ObjectOutputStream ObjectInputStream])
(import '[java.net Socket])
(import '[javax.tools ToolProvider DiagnosticCollector])
(import 'java.util.concurrent.Executors)
(import '[org.apache.commons.io IOUtils])
(require '[clojure.java.io :as io])

(load-file "commands.clj")

(defn jvm-pid
  "Get the PID of the current JVM. Not portable. Use with caution."
  []
  (-> (.. ManagementFactory getRuntimeMXBean getName)
      (clojure.string/split #"@")
      first))

(defn jar-path [jar-name]
  (.getPath (io/resource jar-name)))

(defonce classpath (System/getProperty "java.class.path"))
(defonce bootjar-path (jar-path "btrace-boot.jar"))
(defonce agentjar-path (jar-path "btrace-agent.jar"))
(defonce clientjar-path (jar-path "btrace-client.jar"))
(defonce toolsjar-path (clojure.string/join java.io.File/separator
                          [(System/getProperty "java.home") ".." "lib" "tools.jar"]))


;; ============================================================================
;; Agent
;; ============================================================================
;; for testing purpose
(defonce agent-args "port=3030,debug=true,unsafe=true,trackRetransforms=true,bootClassPath=/Users/myuen/.m2/repository/com/sun/tools/btrace/btrace-boot/1.2.5.1/btrace-boot-1.2.5.1.jar,systemClassP$
th=/Users/myuen/.jenv/versions/oracle64-1.8.0.25/lib/tools.jar,probeDescPath=.")

(defn attach-jvm [^String pid]
  (VirtualMachine/attach pid))

(defn attach-current-jvm []
  (attach-jvm (jvm-pid)))

(defn load-agent
  "Load btrace agent. TODO: Need to move agent args as parameters"
  []
  (let [vm (attach-current-jvm)]
    (.loadAgent vm agentjar-path agent-args)))


;; ============================================================================
;; Compilation (Not easily doable). I will need to copy a lot of *.java files
;; from btrace. Might circle back.
;; ============================================================================
; (defn compile-btrace
;   []
;   (let [file (File. "Memory.java")
;         out (PrintWriter. System/out)
;         build-classpath (clojure.string/join File/pathSeparator [classpath clientjar-path])
;         compiler (ToolProvider/getSystemJavaCompiler)
;         std-manager (.getStandardFileManager compiler nil nil nil)
;         manager (MemoryJavaFileManager. std-manager nil)
;         comp-units (.getJavaFileObjects std-manager (into-array File [file]))
;         diagnostics (DiagnosticCollector.)
;         opts ["-Xlint:all" "-g:lines" "-deprecation" "-source" "1.6"
;               "-target" "1.6" "-sourcepath" "." "-classpath" build-classpath]
;         javac-task (.getTask compiler out manager diagnostics opts nil comp-units)]
;     ))

;; ============================================================================
;; Comm
;; ============================================================================
(defonce membytecode (IOUtils/toByteArray (io/input-stream "Memory.class")))

(defn submit []
  (let [bytecode (IOUtils/toByteArray (io/input-stream "Memory.class"))
        sock (Socket. "localhost" 3030)
        oos (ObjectOutputStream. (.getOutputStream sock))
        ois (ObjectInputStream. (.getInputStream sock))
        ic (InstrumentCommand. bytecode)
        printThreadExec (Executors/newSingleThreadExecutor)]
    (writebytes ic oos)
    (.start (Thread. #(loop []
                        ; TODO polymorphic dispatch
                        (let [cmd-type (.readByte ois)
                              runtime (.readLong ois)
                              len (.readInt ois)
                              buffer (byte-array len)
                              message (.read ois buffer 0 len)]
                          (println (String. buffer "utf-8")))
                        (recur))))
    ; (.writeByte oos (byte 3))
    ; (.writeInt oos (count bytecode))
    ; (.write oos bytecode)
    ; (.writeInt oos (byte 0))
    ; (.flush oos)
    sock))
