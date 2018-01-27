        Jump         $$main                    
        DLabel       $eat-location-zero        
        DataZ        8                         
        DLabel       $print-format-integer     
        DataC        37                        %% "%d"
        DataC        100                       
        DataC        0                         
        DLabel       $print-format-floating    
        DataC        37                        %% "%g"
        DataC        103                       
        DataC        0                         
        DLabel       $print-format-boolean     
        DataC        37                        %% "%s"
        DataC        115                       
        DataC        0                         
        DLabel       $print-format-character   
        DataC        37                        %% "%c"
        DataC        99                        
        DataC        0                         
        DLabel       $print-format-string      
        DataC        37                        %% "%s"
        DataC        115                       
        DataC        0                         
        DLabel       $print-format-newline     
        DataC        10                        %% "\n"
        DataC        0                         
        DLabel       $print-format-tab         
        DataC        9                         %% "\t"
        DataC        0                         
        DLabel       $print-format-space       
        DataC        32                        %% " "
        DataC        0                         
        DLabel       $boolean-true-string      
        DataC        116                       %% "true"
        DataC        114                       
        DataC        117                       
        DataC        101                       
        DataC        0                         
        DLabel       $boolean-false-string     
        DataC        102                       %% "false"
        DataC        97                        
        DataC        108                       
        DataC        115                       
        DataC        101                       
        DataC        0                         
        DLabel       $errors-general-message   
        DataC        82                        %% "Runtime error: %s\n"
        DataC        117                       
        DataC        110                       
        DataC        116                       
        DataC        105                       
        DataC        109                       
        DataC        101                       
        DataC        32                        
        DataC        101                       
        DataC        114                       
        DataC        114                       
        DataC        111                       
        DataC        114                       
        DataC        58                        
        DataC        32                        
        DataC        37                        
        DataC        115                       
        DataC        10                        
        DataC        0                         
        Label        $$general-runtime-error   
        PushD        $errors-general-message   
        Printf                                 
        Halt                                   
        DLabel       $errors-int-divide-by-zero 
        DataC        105                       %% "integer divide by zero"
        DataC        110                       
        DataC        116                       
        DataC        101                       
        DataC        103                       
        DataC        101                       
        DataC        114                       
        DataC        32                        
        DataC        100                       
        DataC        105                       
        DataC        118                       
        DataC        105                       
        DataC        100                       
        DataC        101                       
        DataC        32                        
        DataC        98                        
        DataC        121                       
        DataC        32                        
        DataC        122                       
        DataC        101                       
        DataC        114                       
        DataC        111                       
        DataC        0                         
        Label        $$i-divide-by-zero        
        PushD        $errors-int-divide-by-zero 
        Jump         $$general-runtime-error   
        DLabel       $errors-float-divide-by-zero 
        DataC        102                       %% "float divide by zero"
        DataC        108                       
        DataC        111                       
        DataC        97                        
        DataC        116                       
        DataC        32                        
        DataC        100                       
        DataC        105                       
        DataC        118                       
        DataC        105                       
        DataC        100                       
        DataC        101                       
        DataC        32                        
        DataC        98                        
        DataC        121                       
        DataC        32                        
        DataC        122                       
        DataC        101                       
        DataC        114                       
        DataC        111                       
        DataC        0                         
        Label        $$f-divide-by-zero        
        PushD        $errors-float-divide-by-zero 
        Jump         $$general-runtime-error   
        DLabel       $usable-memory-start      
        DLabel       $global-memory-block      
        DataZ        89                        
        Label        $$main                    
        PushD        $global-memory-block      
        PushI        0                         
        Add                                    %% x
        PushI        10                        
        StoreI                                 
        PushD        $global-memory-block      
        PushI        4                         
        Add                                    %% y
        PushI        122                       
        StoreC                                 
        PushD        $global-memory-block      
        PushI        0                         
        Add                                    %% x
        PushI        -25                       
        StoreI                                 
        PushD        $global-memory-block      
        PushI        0                         
        Add                                    %% x
        LoadI                                  
        PushD        $print-format-integer     
        Printf                                 
        PushD        $print-format-space       
        Printf                                 
        PushD        $global-memory-block      
        PushI        4                         
        Add                                    %% y
        LoadC                                  
        PushD        $print-format-character   
        Printf                                 
        PushD        $print-format-space       
        Printf                                 
        PushD        $global-memory-block      
        PushI        0                         
        Add                                    %% x
        LoadI                                  
        PushI        2                         
        Add                                    
        PushD        $print-format-integer     
        Printf                                 
        PushD        $print-format-space       
        Printf                                 
        PushD        $print-format-newline     
        Printf                                 
        PushD        $global-memory-block      
        PushI        5                         
        Add                                    %% z
        DLabel       -string-constant-1-       
        DataC        104                       %% "hello"
        DataC        101                       
        DataC        108                       
        DataC        108                       
        DataC        111                       
        DataC        0                         
        PushD        -string-constant-1-       
        StoreI                                 
        PushD        $global-memory-block      
        PushI        9                         
        Add                                    %% w
        PushD        $global-memory-block      
        PushI        5                         
        Add                                    %% z
        LoadI                                  
        StoreI                                 
        DLabel       -string-constant-2-       
        DataC        67                        %% "Comparison of z and w:"
        DataC        111                       
        DataC        109                       
        DataC        112                       
        DataC        97                        
        DataC        114                       
        DataC        105                       
        DataC        115                       
        DataC        111                       
        DataC        110                       
        DataC        32                        
        DataC        111                       
        DataC        102                       
        DataC        32                        
        DataC        122                       
        DataC        32                        
        DataC        97                        
        DataC        110                       
        DataC        100                       
        DataC        32                        
        DataC        119                       
        DataC        58                        
        DataC        0                         
        PushD        -string-constant-2-       
        PushD        $print-format-string      
        Printf                                 
        PushD        $print-format-space       
        Printf                                 
        Label        -compare-3-arg1           
        PushD        $global-memory-block      
        PushI        5                         
        Add                                    %% z
        LoadI                                  
        Label        -compare-3-arg2           
        PushD        $global-memory-block      
        PushI        9                         
        Add                                    %% w
        LoadI                                  
        Label        -compare-3-sub            
        Subtract                               
        JumpFalse    -compare-3-true           
        Jump         -compare-3-false          
        Label        -compare-3-true           
        PushI        1                         
        Jump         -compare-3-join           
        Label        -compare-3-false          
        PushI        0                         
        Jump         -compare-3-join           
        Label        -compare-3-join           
        JumpTrue     -print-boolean-4-true     
        PushD        $boolean-false-string     
        Jump         -print-boolean-4-join     
        Label        -print-boolean-4-true     
        PushD        $boolean-true-string      
        Label        -print-boolean-4-join     
        PushD        $print-format-boolean     
        Printf                                 
        PushD        $print-format-newline     
        Printf                                 
        DLabel       -string-constant-5-       
        DataC        67                        %% "Comparison of z and hello:"
        DataC        111                       
        DataC        109                       
        DataC        112                       
        DataC        97                        
        DataC        114                       
        DataC        105                       
        DataC        115                       
        DataC        111                       
        DataC        110                       
        DataC        32                        
        DataC        111                       
        DataC        102                       
        DataC        32                        
        DataC        122                       
        DataC        32                        
        DataC        97                        
        DataC        110                       
        DataC        100                       
        DataC        32                        
        DataC        104                       
        DataC        101                       
        DataC        108                       
        DataC        108                       
        DataC        111                       
        DataC        58                        
        DataC        0                         
        PushD        -string-constant-5-       
        PushD        $print-format-string      
        Printf                                 
        Label        -compare-7-arg1           
        PushD        $global-memory-block      
        PushI        5                         
        Add                                    %% z
        LoadI                                  
        Label        -compare-7-arg2           
        DLabel       -string-constant-6-       
        DataC        104                       %% "hello"
        DataC        101                       
        DataC        108                       
        DataC        108                       
        DataC        111                       
        DataC        0                         
        PushD        -string-constant-6-       
        Label        -compare-7-sub            
        Subtract                               
        JumpFalse    -compare-7-true           
        Jump         -compare-7-false          
        Label        -compare-7-true           
        PushI        1                         
        Jump         -compare-7-join           
        Label        -compare-7-false          
        PushI        0                         
        Jump         -compare-7-join           
        Label        -compare-7-join           
        JumpTrue     -print-boolean-8-true     
        PushD        $boolean-false-string     
        Jump         -print-boolean-8-join     
        Label        -print-boolean-8-true     
        PushD        $boolean-true-string      
        Label        -print-boolean-8-join     
        PushD        $print-format-boolean     
        Printf                                 
        PushD        $print-format-newline     
        Printf                                 
        PushD        $print-format-newline     
        Printf                                 
        PushD        $global-memory-block      
        PushI        13                        
        Add                                    %% a
        PushF        -0.000789                 
        StoreF                                 
        PushD        $global-memory-block      
        PushI        21                        
        Add                                    %% b
        PushD        $global-memory-block      
        PushI        13                        
        Add                                    %% a
        LoadF                                  
        PushD        $global-memory-block      
        PushI        13                        
        Add                                    %% a
        LoadF                                  
        FAdd                                   
        PushF        4.000000                  
        FMultiply                              
        PushF        3.000000                  
        Duplicate                              
        JumpFZero    $$f-divide-by-zero        
        FDivide                                
        StoreF                                 
        PushD        $global-memory-block      
        PushI        13                        
        Add                                    %% a
        LoadF                                  
        PushD        $print-format-floating    
        Printf                                 
        PushD        $print-format-tab         
        Printf                                 
        PushD        $global-memory-block      
        PushI        21                        
        Add                                    %% b
        LoadF                                  
        PushD        $print-format-floating    
        Printf                                 
        PushD        $print-format-newline     
        Printf                                 
        PushD        $global-memory-block      
        PushI        29                        
        Add                                    %% e
        PushI        1                         
        StoreC                                 
        PushD        $global-memory-block      
        PushI        30                        
        Add                                    %% f
        PushI        0                         
        StoreC                                 
        PushD        $global-memory-block      
        PushI        31                        
        Add                                    %% g
        Label        -compare-9-arg1           
        PushD        $global-memory-block      
        PushI        29                        
        Add                                    %% e
        LoadC                                  
        Label        -compare-9-arg2           
        PushD        $global-memory-block      
        PushI        30                        
        Add                                    %% f
        LoadC                                  
        Label        -compare-9-sub            
        Subtract                               
        JumpFalse    -compare-9-true           
        Jump         -compare-9-false          
        Label        -compare-9-true           
        PushI        1                         
        Jump         -compare-9-join           
        Label        -compare-9-false          
        PushI        0                         
        Jump         -compare-9-join           
        Label        -compare-9-join           
        StoreC                                 
        PushD        $global-memory-block      
        PushI        32                        
        Add                                    %% h
        Label        -compare-10-arg1          
        PushD        $global-memory-block      
        PushI        31                        
        Add                                    %% g
        LoadC                                  
        Label        -compare-10-arg2          
        PushD        $global-memory-block      
        PushI        30                        
        Add                                    %% f
        LoadC                                  
        Label        -compare-10-sub           
        Subtract                               
        JumpFalse    -compare-10-true          
        Jump         -compare-10-false         
        Label        -compare-10-true          
        PushI        1                         
        Jump         -compare-10-join          
        Label        -compare-10-false         
        PushI        0                         
        Jump         -compare-10-join          
        Label        -compare-10-join          
        StoreC                                 
        DLabel       -string-constant-11-      
        DataC        118                       %% "value of e:"
        DataC        97                        
        DataC        108                       
        DataC        117                       
        DataC        101                       
        DataC        32                        
        DataC        111                       
        DataC        102                       
        DataC        32                        
        DataC        101                       
        DataC        58                        
        DataC        0                         
        PushD        -string-constant-11-      
        PushD        $print-format-string      
        Printf                                 
        PushD        $print-format-space       
        Printf                                 
        PushD        $global-memory-block      
        PushI        29                        
        Add                                    %% e
        LoadC                                  
        JumpTrue     -print-boolean-12-true    
        PushD        $boolean-false-string     
        Jump         -print-boolean-12-join    
        Label        -print-boolean-12-true    
        PushD        $boolean-true-string      
        Label        -print-boolean-12-join    
        PushD        $print-format-boolean     
        Printf                                 
        PushD        $print-format-space       
        Printf                                 
        PushD        $print-format-newline     
        Printf                                 
        DLabel       -string-constant-13-      
        DataC        118                       %% "value of f:"
        DataC        97                        
        DataC        108                       
        DataC        117                       
        DataC        101                       
        DataC        32                        
        DataC        111                       
        DataC        102                       
        DataC        32                        
        DataC        102                       
        DataC        58                        
        DataC        0                         
        PushD        -string-constant-13-      
        PushD        $print-format-string      
        Printf                                 
        PushD        $print-format-space       
        Printf                                 
        PushD        $global-memory-block      
        PushI        30                        
        Add                                    %% f
        LoadC                                  
        JumpTrue     -print-boolean-14-true    
        PushD        $boolean-false-string     
        Jump         -print-boolean-14-join    
        Label        -print-boolean-14-true    
        PushD        $boolean-true-string      
        Label        -print-boolean-14-join    
        PushD        $print-format-boolean     
        Printf                                 
        PushD        $print-format-space       
        Printf                                 
        PushD        $print-format-newline     
        Printf                                 
        DLabel       -string-constant-15-      
        DataC        118                       %% "value of g:"
        DataC        97                        
        DataC        108                       
        DataC        117                       
        DataC        101                       
        DataC        32                        
        DataC        111                       
        DataC        102                       
        DataC        32                        
        DataC        103                       
        DataC        58                        
        DataC        0                         
        PushD        -string-constant-15-      
        PushD        $print-format-string      
        Printf                                 
        PushD        $print-format-space       
        Printf                                 
        PushD        $global-memory-block      
        PushI        31                        
        Add                                    %% g
        LoadC                                  
        JumpTrue     -print-boolean-16-true    
        PushD        $boolean-false-string     
        Jump         -print-boolean-16-join    
        Label        -print-boolean-16-true    
        PushD        $boolean-true-string      
        Label        -print-boolean-16-join    
        PushD        $print-format-boolean     
        Printf                                 
        PushD        $print-format-newline     
        Printf                                 
        DLabel       -string-constant-17-      
        DataC        118                       %% "value of h:"
        DataC        97                        
        DataC        108                       
        DataC        117                       
        DataC        101                       
        DataC        32                        
        DataC        111                       
        DataC        102                       
        DataC        32                        
        DataC        104                       
        DataC        58                        
        DataC        0                         
        PushD        -string-constant-17-      
        PushD        $print-format-string      
        Printf                                 
        PushD        $print-format-space       
        Printf                                 
        PushD        $global-memory-block      
        PushI        32                        
        Add                                    %% h
        LoadC                                  
        JumpTrue     -print-boolean-18-true    
        PushD        $boolean-false-string     
        Jump         -print-boolean-18-join    
        Label        -print-boolean-18-true    
        PushD        $boolean-true-string      
        Label        -print-boolean-18-join    
        PushD        $print-format-boolean     
        Printf                                 
        PushD        $print-format-space       
        Printf                                 
        PushD        $print-format-newline     
        Printf                                 
        PushD        $global-memory-block      
        PushI        29                        
        Add                                    %% e
        PushI        0                         
        StoreC                                 
        PushD        $global-memory-block      
        PushI        30                        
        Add                                    %% f
        PushD        $global-memory-block      
        PushI        30                        
        Add                                    %% f
        LoadC                                  
        Nop                                    
        StoreC                                 
        PushD        $global-memory-block      
        PushI        31                        
        Add                                    %% g
        Label        -compare-19-arg1          
        PushD        $global-memory-block      
        PushI        29                        
        Add                                    %% e
        LoadC                                  
        Label        -compare-19-arg2          
        PushI        0                         
        Label        -compare-19-sub           
        Subtract                               
        JumpFalse    -compare-19-true          
        Jump         -compare-19-false         
        Label        -compare-19-true          
        PushI        1                         
        Jump         -compare-19-join          
        Label        -compare-19-false         
        PushI        0                         
        Jump         -compare-19-join          
        Label        -compare-19-join          
        Nop                                    
        Nop                                    
        StoreC                                 
        PushD        $global-memory-block      
        PushI        32                        
        Add                                    %% h
        PushI        1                         
        StoreC                                 
        DLabel       -string-constant-20-      
        DataC        110                       %% "new value of e:"
        DataC        101                       
        DataC        119                       
        DataC        32                        
        DataC        118                       
        DataC        97                        
        DataC        108                       
        DataC        117                       
        DataC        101                       
        DataC        32                        
        DataC        111                       
        DataC        102                       
        DataC        32                        
        DataC        101                       
        DataC        58                        
        DataC        0                         
        PushD        -string-constant-20-      
        PushD        $print-format-string      
        Printf                                 
        PushD        $print-format-space       
        Printf                                 
        PushD        $global-memory-block      
        PushI        29                        
        Add                                    %% e
        LoadC                                  
        JumpTrue     -print-boolean-21-true    
        PushD        $boolean-false-string     
        Jump         -print-boolean-21-join    
        Label        -print-boolean-21-true    
        PushD        $boolean-true-string      
        Label        -print-boolean-21-join    
        PushD        $print-format-boolean     
        Printf                                 
        PushD        $print-format-space       
        Printf                                 
        PushD        $print-format-newline     
        Printf                                 
        DLabel       -string-constant-22-      
        DataC        110                       %% "new value of f:"
        DataC        101                       
        DataC        119                       
        DataC        32                        
        DataC        118                       
        DataC        97                        
        DataC        108                       
        DataC        117                       
        DataC        101                       
        DataC        32                        
        DataC        111                       
        DataC        102                       
        DataC        32                        
        DataC        102                       
        DataC        58                        
        DataC        0                         
        PushD        -string-constant-22-      
        PushD        $print-format-string      
        Printf                                 
        PushD        $print-format-space       
        Printf                                 
        PushD        $global-memory-block      
        PushI        30                        
        Add                                    %% f
        LoadC                                  
        JumpTrue     -print-boolean-23-true    
        PushD        $boolean-false-string     
        Jump         -print-boolean-23-join    
        Label        -print-boolean-23-true    
        PushD        $boolean-true-string      
        Label        -print-boolean-23-join    
        PushD        $print-format-boolean     
        Printf                                 
        PushD        $print-format-space       
        Printf                                 
        PushD        $print-format-newline     
        Printf                                 
        DLabel       -string-constant-24-      
        DataC        110                       %% "new value of g:"
        DataC        101                       
        DataC        119                       
        DataC        32                        
        DataC        118                       
        DataC        97                        
        DataC        108                       
        DataC        117                       
        DataC        101                       
        DataC        32                        
        DataC        111                       
        DataC        102                       
        DataC        32                        
        DataC        103                       
        DataC        58                        
        DataC        0                         
        PushD        -string-constant-24-      
        PushD        $print-format-string      
        Printf                                 
        PushD        $print-format-space       
        Printf                                 
        PushD        $global-memory-block      
        PushI        31                        
        Add                                    %% g
        LoadC                                  
        JumpTrue     -print-boolean-25-true    
        PushD        $boolean-false-string     
        Jump         -print-boolean-25-join    
        Label        -print-boolean-25-true    
        PushD        $boolean-true-string      
        Label        -print-boolean-25-join    
        PushD        $print-format-boolean     
        Printf                                 
        PushD        $print-format-newline     
        Printf                                 
        DLabel       -string-constant-26-      
        DataC        110                       %% "new value of h:"
        DataC        101                       
        DataC        119                       
        DataC        32                        
        DataC        118                       
        DataC        97                        
        DataC        108                       
        DataC        117                       
        DataC        101                       
        DataC        32                        
        DataC        111                       
        DataC        102                       
        DataC        32                        
        DataC        104                       
        DataC        58                        
        DataC        0                         
        PushD        -string-constant-26-      
        PushD        $print-format-string      
        Printf                                 
        PushD        $print-format-space       
        Printf                                 
        PushD        $global-memory-block      
        PushI        32                        
        Add                                    %% h
        LoadC                                  
        JumpTrue     -print-boolean-27-true    
        PushD        $boolean-false-string     
        Jump         -print-boolean-27-join    
        Label        -print-boolean-27-true    
        PushD        $boolean-true-string      
        Label        -print-boolean-27-join    
        PushD        $print-format-boolean     
        Printf                                 
        PushD        $print-format-space       
        Printf                                 
        PushD        $print-format-newline     
        Printf                                 
        PushD        $global-memory-block      
        PushI        33                        
        Add                                    %% _
        PushI        114                       
        StoreC                                 
        PushD        $global-memory-block      
        PushI        34                        
        Add                                    %% x_4_g_16$$ZZZ
        PushI        115                       
        StoreC                                 
        PushD        $global-memory-block      
        PushI        35                        
        Add                                    %% _$999
        DLabel       -string-constant-28-      
        DataC        103                       %% "goodbye"
        DataC        111                       
        DataC        111                       
        DataC        100                       
        DataC        98                        
        DataC        121                       
        DataC        101                       
        DataC        0                         
        PushD        -string-constant-28-      
        StoreI                                 
        Label        -compare-29-arg1          
        PushD        $global-memory-block      
        PushI        33                        
        Add                                    %% _
        LoadC                                  
        Label        -compare-29-arg2          
        PushD        $global-memory-block      
        PushI        34                        
        Add                                    %% x_4_g_16$$ZZZ
        LoadC                                  
        Label        -compare-29-sub           
        Subtract                               
        JumpPos      -compare-29-true          
        Jump         -compare-29-false         
        Label        -compare-29-true          
        PushI        1                         
        Jump         -compare-29-join          
        Label        -compare-29-false         
        PushI        0                         
        Jump         -compare-29-join          
        Label        -compare-29-join          
        JumpTrue     -print-boolean-33-true    
        PushD        $boolean-false-string     
        Jump         -print-boolean-33-join    
        Label        -print-boolean-33-true    
        PushD        $boolean-true-string      
        Label        -print-boolean-33-join    
        PushD        $print-format-boolean     
        Printf                                 
        PushD        $print-format-space       
        Printf                                 
        Label        -compare-30-arg1          
        PushD        $global-memory-block      
        PushI        33                        
        Add                                    %% _
        LoadC                                  
        Label        -compare-30-arg2          
        PushD        $global-memory-block      
        PushI        34                        
        Add                                    %% x_4_g_16$$ZZZ
        LoadC                                  
        Label        -compare-30-sub           
        Subtract                               
        JumpNeg      -compare-30-true          
        Jump         -compare-30-false         
        Label        -compare-30-true          
        PushI        1                         
        Jump         -compare-30-join          
        Label        -compare-30-false         
        PushI        0                         
        Jump         -compare-30-join          
        Label        -compare-30-join          
        JumpTrue     -print-boolean-34-true    
        PushD        $boolean-false-string     
        Jump         -print-boolean-34-join    
        Label        -print-boolean-34-true    
        PushD        $boolean-true-string      
        Label        -print-boolean-34-join    
        PushD        $print-format-boolean     
        Printf                                 
        PushD        $print-format-space       
        Printf                                 
        Label        -compare-31-arg1          
        PushD        $global-memory-block      
        PushI        33                        
        Add                                    %% _
        LoadC                                  
        Label        -compare-31-arg2          
        PushD        $global-memory-block      
        PushI        34                        
        Add                                    %% x_4_g_16$$ZZZ
        LoadC                                  
        Label        -compare-31-sub           
        Subtract                               
        JumpFalse    -compare-31-true          
        Jump         -compare-31-false         
        Label        -compare-31-true          
        PushI        1                         
        Jump         -compare-31-join          
        Label        -compare-31-false         
        PushI        0                         
        Jump         -compare-31-join          
        Label        -compare-31-join          
        JumpTrue     -print-boolean-35-true    
        PushD        $boolean-false-string     
        Jump         -print-boolean-35-join    
        Label        -print-boolean-35-true    
        PushD        $boolean-true-string      
        Label        -print-boolean-35-join    
        PushD        $print-format-boolean     
        Printf                                 
        PushD        $print-format-space       
        Printf                                 
        Label        -compare-32-arg1          
        PushD        $global-memory-block      
        PushI        33                        
        Add                                    %% _
        LoadC                                  
        Label        -compare-32-arg2          
        PushD        $global-memory-block      
        PushI        34                        
        Add                                    %% x_4_g_16$$ZZZ
        LoadC                                  
        Label        -compare-32-sub           
        Subtract                               
        JumpTrue     -compare-32-true          
        Jump         -compare-32-false         
        Label        -compare-32-true          
        PushI        1                         
        Jump         -compare-32-join          
        Label        -compare-32-false         
        PushI        0                         
        Jump         -compare-32-join          
        Label        -compare-32-join          
        JumpTrue     -print-boolean-36-true    
        PushD        $boolean-false-string     
        Jump         -print-boolean-36-join    
        Label        -print-boolean-36-true    
        PushD        $boolean-true-string      
        Label        -print-boolean-36-join    
        PushD        $print-format-boolean     
        Printf                                 
        PushD        $print-format-space       
        Printf                                 
        PushD        $print-format-newline     
        Printf                                 
        PushD        $global-memory-block      
        PushI        34                        
        Add                                    %% x_4_g_16$$ZZZ
        PushI        83                        
        StoreC                                 
        Label        -compare-37-arg1          
        PushD        $global-memory-block      
        PushI        33                        
        Add                                    %% _
        LoadC                                  
        Label        -compare-37-arg2          
        PushD        $global-memory-block      
        PushI        34                        
        Add                                    %% x_4_g_16$$ZZZ
        LoadC                                  
        Label        -compare-37-sub           
        Subtract                               
        JumpPos      -compare-37-true          
        Jump         -compare-37-false         
        Label        -compare-37-true          
        PushI        1                         
        Jump         -compare-37-join          
        Label        -compare-37-false         
        PushI        0                         
        Jump         -compare-37-join          
        Label        -compare-37-join          
        JumpTrue     -print-boolean-41-true    
        PushD        $boolean-false-string     
        Jump         -print-boolean-41-join    
        Label        -print-boolean-41-true    
        PushD        $boolean-true-string      
        Label        -print-boolean-41-join    
        PushD        $print-format-boolean     
        Printf                                 
        PushD        $print-format-space       
        Printf                                 
        Label        -compare-38-arg1          
        PushD        $global-memory-block      
        PushI        33                        
        Add                                    %% _
        LoadC                                  
        Label        -compare-38-arg2          
        PushD        $global-memory-block      
        PushI        34                        
        Add                                    %% x_4_g_16$$ZZZ
        LoadC                                  
        Label        -compare-38-sub           
        Subtract                               
        JumpNeg      -compare-38-true          
        Jump         -compare-38-false         
        Label        -compare-38-true          
        PushI        1                         
        Jump         -compare-38-join          
        Label        -compare-38-false         
        PushI        0                         
        Jump         -compare-38-join          
        Label        -compare-38-join          
        JumpTrue     -print-boolean-42-true    
        PushD        $boolean-false-string     
        Jump         -print-boolean-42-join    
        Label        -print-boolean-42-true    
        PushD        $boolean-true-string      
        Label        -print-boolean-42-join    
        PushD        $print-format-boolean     
        Printf                                 
        PushD        $print-format-space       
        Printf                                 
        Label        -compare-39-arg1          
        PushD        $global-memory-block      
        PushI        33                        
        Add                                    %% _
        LoadC                                  
        Label        -compare-39-arg2          
        PushD        $global-memory-block      
        PushI        34                        
        Add                                    %% x_4_g_16$$ZZZ
        LoadC                                  
        Label        -compare-39-sub           
        Subtract                               
        JumpFalse    -compare-39-true          
        Jump         -compare-39-false         
        Label        -compare-39-true          
        PushI        1                         
        Jump         -compare-39-join          
        Label        -compare-39-false         
        PushI        0                         
        Jump         -compare-39-join          
        Label        -compare-39-join          
        JumpTrue     -print-boolean-43-true    
        PushD        $boolean-false-string     
        Jump         -print-boolean-43-join    
        Label        -print-boolean-43-true    
        PushD        $boolean-true-string      
        Label        -print-boolean-43-join    
        PushD        $print-format-boolean     
        Printf                                 
        PushD        $print-format-space       
        Printf                                 
        Label        -compare-40-arg1          
        PushD        $global-memory-block      
        PushI        33                        
        Add                                    %% _
        LoadC                                  
        Label        -compare-40-arg2          
        PushD        $global-memory-block      
        PushI        34                        
        Add                                    %% x_4_g_16$$ZZZ
        LoadC                                  
        Label        -compare-40-sub           
        Subtract                               
        JumpTrue     -compare-40-true          
        Jump         -compare-40-false         
        Label        -compare-40-true          
        PushI        1                         
        Jump         -compare-40-join          
        Label        -compare-40-false         
        PushI        0                         
        Jump         -compare-40-join          
        Label        -compare-40-join          
        JumpTrue     -print-boolean-44-true    
        PushD        $boolean-false-string     
        Jump         -print-boolean-44-join    
        Label        -print-boolean-44-true    
        PushD        $boolean-true-string      
        Label        -print-boolean-44-join    
        PushD        $print-format-boolean     
        Printf                                 
        PushD        $print-format-space       
        Printf                                 
        PushD        $print-format-newline     
        Printf                                 
        PushD        $global-memory-block      
        PushI        35                        
        Add                                    %% _$999
        LoadI                                  
        PushD        $print-format-string      
        Printf                                 
        PushD        $global-memory-block      
        PushI        35                        
        Add                                    %% _$999
        LoadI                                  
        PushD        $print-format-string      
        Printf                                 
        PushD        $global-memory-block      
        PushI        35                        
        Add                                    %% _$999
        LoadI                                  
        PushD        $print-format-string      
        Printf                                 
        PushD        $print-format-newline     
        Printf                                 
        PushD        $global-memory-block      
        PushI        39                        
        Add                                    %% x1
        PushI        444                       
        StoreI                                 
        PushD        $global-memory-block      
        PushI        39                        
        Add                                    %% x1
        LoadI                                  
        PushD        $print-format-integer     
        Printf                                 
        PushD        $print-format-newline     
        Printf                                 
        PushD        $global-memory-block      
        PushI        39                        
        Add                                    %% x1
        PushD        $global-memory-block      
        PushI        39                        
        Add                                    %% x1
        LoadI                                  
        PushD        $global-memory-block      
        PushI        39                        
        Add                                    %% x1
        LoadI                                  
        Add                                    
        StoreI                                 
        PushD        $global-memory-block      
        PushI        39                        
        Add                                    %% x1
        LoadI                                  
        PushD        $print-format-integer     
        Printf                                 
        PushD        $print-format-newline     
        Printf                                 
        PushD        $global-memory-block      
        PushI        39                        
        Add                                    %% x1
        PushD        $global-memory-block      
        PushI        39                        
        Add                                    %% x1
        LoadI                                  
        PushI        222                       
        Subtract                               
        PushI        144                       
        PushD        $global-memory-block      
        PushI        39                        
        Add                                    %% x1
        LoadI                                  
        Subtract                               
        PushI        8                         
        Duplicate                              
        JumpFalse    $$i-divide-by-zero        
        Divide                                 
        Multiply                               
        StoreI                                 
        PushD        $global-memory-block      
        PushI        39                        
        Add                                    %% x1
        LoadI                                  
        PushD        $print-format-integer     
        Printf                                 
        PushD        $print-format-newline     
        Printf                                 
        PushD        $global-memory-block      
        PushI        43                        
        Add                                    %% const1
        PushF        4000.321000               
        StoreF                                 
        PushI        1                         
        PushD        $print-format-integer     
        Printf                                 
        PushD        $print-format-tab         
        Printf                                 
        PushI        2                         
        PushD        $print-format-integer     
        Printf                                 
        PushD        $print-format-tab         
        Printf                                 
        PushI        3                         
        PushD        $print-format-integer     
        Printf                                 
        PushD        $print-format-tab         
        Printf                                 
        PushI        4                         
        PushD        $print-format-integer     
        Printf                                 
        PushD        $print-format-newline     
        Printf                                 
        PushF        5.500000                  
        PushD        $print-format-floating    
        Printf                                 
        PushD        $print-format-tab         
        Printf                                 
        PushF        -6.600000                 
        PushD        $print-format-floating    
        Printf                                 
        PushD        $print-format-tab         
        Printf                                 
        PushF        7.700000                  
        PushD        $print-format-floating    
        Printf                                 
        PushD        $print-format-tab         
        Printf                                 
        PushF        -8.800000                 
        PushD        $print-format-floating    
        Printf                                 
        PushD        $print-format-newline     
        Printf                                 
        PushI        57                        
        PushD        $print-format-character   
        Printf                                 
        PushD        $print-format-tab         
        Printf                                 
        PushI        97                        
        PushD        $print-format-character   
        Printf                                 
        PushD        $print-format-tab         
        Printf                                 
        PushI        98                        
        PushD        $print-format-character   
        Printf                                 
        PushD        $print-format-tab         
        Printf                                 
        PushI        99                        
        PushD        $print-format-character   
        Printf                                 
        PushD        $print-format-newline     
        Printf                                 
        DLabel       -string-constant-45-      
        DataC        116                       %% "this will be printed"
        DataC        104                       
        DataC        105                       
        DataC        115                       
        DataC        32                        
        DataC        119                       
        DataC        105                       
        DataC        108                       
        DataC        108                       
        DataC        32                        
        DataC        98                        
        DataC        101                       
        DataC        32                        
        DataC        112                       
        DataC        114                       
        DataC        105                       
        DataC        110                       
        DataC        116                       
        DataC        101                       
        DataC        100                       
        DataC        0                         
        PushD        -string-constant-45-      
        PushD        $print-format-string      
        Printf                                 
        PushD        $print-format-newline     
        Printf                                 
        PushI        1                         
        JumpTrue     -print-boolean-46-true    
        PushD        $boolean-false-string     
        Jump         -print-boolean-46-join    
        Label        -print-boolean-46-true    
        PushD        $boolean-true-string      
        Label        -print-boolean-46-join    
        PushD        $print-format-boolean     
        Printf                                 
        PushD        $print-format-space       
        Printf                                 
        PushI        0                         
        JumpTrue     -print-boolean-47-true    
        PushD        $boolean-false-string     
        Jump         -print-boolean-47-join    
        Label        -print-boolean-47-true    
        PushD        $boolean-true-string      
        Label        -print-boolean-47-join    
        PushD        $print-format-boolean     
        Printf                                 
        PushD        $print-format-space       
        Printf                                 
        PushD        $print-format-newline     
        Printf                                 
        PushD        $global-memory-block      
        PushI        51                        
        Add                                    %% mario
        PushI        1096                      
        PushI        127                       
        BTAnd                                  
        StoreC                                 
        PushD        $global-memory-block      
        PushI        52                        
        Add                                    %% luigi
        PushI        8293                      
        PushI        127                       
        BTAnd                                  
        StoreC                                 
        PushD        $global-memory-block      
        PushI        53                        
        Add                                    %% wario
        PushI        65657                     
        PushI        127                       
        BTAnd                                  
        StoreC                                 
        PushD        $global-memory-block      
        PushI        54                        
        Add                                    %% peach
        PushI        32                        
        ConvertF                               
        StoreF                                 
        PushD        $global-memory-block      
        PushI        62                        
        Add                                    %% BOWSER
        PushF        144.000000                
        PushF        72.600000                 
        FSubtract                              
        PushF        -16.400000                
        PushF        11.500000                 
        FMultiply                              
        Duplicate                              
        JumpFZero    $$f-divide-by-zero        
        FDivide                                
        StoreF                                 
        PushD        $global-memory-block      
        PushI        51                        
        Add                                    %% mario
        LoadC                                  
        PushD        $print-format-character   
        Printf                                 
        PushD        $print-format-newline     
        Printf                                 
        PushD        $global-memory-block      
        PushI        52                        
        Add                                    %% luigi
        LoadC                                  
        PushD        $print-format-character   
        Printf                                 
        PushD        $print-format-newline     
        Printf                                 
        PushD        $global-memory-block      
        PushI        53                        
        Add                                    %% wario
        LoadC                                  
        PushD        $print-format-character   
        Printf                                 
        PushD        $print-format-newline     
        Printf                                 
        PushD        $global-memory-block      
        PushI        54                        
        Add                                    %% peach
        LoadF                                  
        PushD        $print-format-floating    
        Printf                                 
        PushD        $print-format-newline     
        Printf                                 
        PushD        $global-memory-block      
        PushI        62                        
        Add                                    %% BOWSER
        LoadF                                  
        PushD        $print-format-floating    
        Printf                                 
        PushD        $print-format-newline     
        Printf                                 
        PushD        $global-memory-block      
        PushI        70                        
        Add                                    %% toad
        PushI        -190                      
        PushI        127                       
        BTAnd                                  
        StoreC                                 
        PushD        $global-memory-block      
        PushI        71                        
        Add                                    %% yOsHi
        PushI        -135                      
        PushI        127                       
        BTAnd                                  
        StoreC                                 
        PushD        $global-memory-block      
        PushI        72                        
        Add                                    %% koopA
        PushI        0                         
        PushI        155                       
        Subtract                               
        PushI        256                       
        Subtract                               
        PushI        1024                      
        Subtract                               
        PushI        4096                      
        Subtract                               
        PushI        127                       
        BTAnd                                  
        StoreC                                 
        PushD        $global-memory-block      
        PushI        70                        
        Add                                    %% toad
        LoadC                                  
        PushD        $print-format-character   
        Printf                                 
        PushD        $global-memory-block      
        PushI        71                        
        Add                                    %% yOsHi
        LoadC                                  
        PushD        $print-format-character   
        Printf                                 
        PushD        $global-memory-block      
        PushI        72                        
        Add                                    %% koopA
        LoadC                                  
        PushD        $print-format-character   
        Printf                                 
        PushD        $print-format-newline     
        Printf                                 
        PushD        $global-memory-block      
        PushI        73                        
        Add                                    %% _var_
        PushI        3                         
        PushI        4                         
        PushI        2                         
        Duplicate                              
        JumpFalse    $$i-divide-by-zero        
        Divide                                 
        Add                                    
        PushI        127                       
        BTAnd                                  
        Nop                                    
        PushI        9                         
        Add                                    
        ConvertF                               
        PushF        8.600000                  
        FSubtract                              
        ConvertI                               
        StoreI                                 
        PushD        $global-memory-block      
        PushI        73                        
        Add                                    %% _var_
        LoadI                                  
        PushD        $print-format-integer     
        Printf                                 
        PushD        $print-format-newline     
        Printf                                 
        PushD        $global-memory-block      
        PushI        77                        
        Add                                    %% _var2_
        DLabel       -string-constant-48-      
        DataC        89                        %% "You'll never see it coming"
        DataC        111                       
        DataC        117                       
        DataC        39                        
        DataC        108                       
        DataC        108                       
        DataC        32                        
        DataC        110                       
        DataC        101                       
        DataC        118                       
        DataC        101                       
        DataC        114                       
        DataC        32                        
        DataC        115                       
        DataC        101                       
        DataC        101                       
        DataC        32                        
        DataC        105                       
        DataC        116                       
        DataC        32                        
        DataC        99                        
        DataC        111                       
        DataC        109                       
        DataC        105                       
        DataC        110                       
        DataC        103                       
        DataC        0                         
        PushD        -string-constant-48-      
        Nop                                    
        StoreI                                 
        PushD        $global-memory-block      
        PushI        81                        
        Add                                    %% _var3_
        PushD        $global-memory-block      
        PushI        77                        
        Add                                    %% _var2_
        LoadI                                  
        Nop                                    
        Nop                                    
        StoreI                                 
        PushD        $global-memory-block      
        PushI        77                        
        Add                                    %% _var2_
        LoadI                                  
        PushD        $print-format-string      
        Printf                                 
        PushD        $print-format-newline     
        Printf                                 
        PushD        $global-memory-block      
        PushI        81                        
        Add                                    %% _var3_
        LoadI                                  
        PushD        $print-format-string      
        Printf                                 
        PushD        $print-format-newline     
        Printf                                 
        DLabel       -string-constant-49-      
        DataC        104                       %% "hi"
        DataC        105                       
        DataC        0                         
        PushD        -string-constant-49-      
        PushD        $print-format-string      
        Printf                                 
        PushD        $print-format-newline     
        Printf                                 
        PushD        $global-memory-block      
        PushI        85                        
        Add                                    %% y3
        PushI        3                         
        PushI        2                         
        PushI        6                         
        Multiply                               
        Add                                    
        StoreI                                 
        PushD        $global-memory-block      
        PushI        85                        
        Add                                    %% y3
        LoadI                                  
        PushD        $print-format-integer     
        Printf                                 
        PushD        $print-format-space       
        Printf                                 
        PushD        $global-memory-block      
        PushI        85                        
        Add                                    %% y3
        PushI        3                         
        PushI        2                         
        Add                                    
        PushI        6                         
        Multiply                               
        StoreI                                 
        PushD        $global-memory-block      
        PushI        85                        
        Add                                    %% y3
        LoadI                                  
        PushD        $print-format-integer     
        Printf                                 
        PushD        $print-format-space       
        Printf                                 
        PushD        $print-format-newline     
        Printf                                 
        PushF        4.000000                  
        PushI        0                         
        ConvertF                               
        Duplicate                              
        JumpFZero    $$f-divide-by-zero        
        FDivide                                
        PushD        $print-format-floating    
        Printf                                 
        PushI        144                       
        PushD        $print-format-integer     
        Printf                                 
        Halt                                   
