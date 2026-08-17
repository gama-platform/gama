bits 64
org 0x08048000

ehdr:                                     ; Elf64_Ehdr
    db  0x7F, "ELF", 2, 1, 1, 0   ;   e_ident
    times 8 db  0
    dw  2                         ;   e_type
    dw  62                        ;   e_machine
    dd  1                         ;   e_version
    dq  _start                    ;   e_entry
    dq  phdr - $$                 ;   e_phoff
    dq  0                         ;   e_shoff
    dd  0                         ;   e_flags
    dw  ehdrsize                  ;   e_ehsize
    dw  phdrsize                  ;   e_phentsize
    dw  1                         ;   e_phnum
    dw  0                         ;   e_shentsize
    dw  0                         ;   e_shnum
    dw  0                         ;   e_shstrndx

ehdrsize equ $ - ehdr

phdr:                                     ; Elf64_Phdr
    dd  1                         ;   p_type
    dd  7                         ;   p_flags
    dq  0                         ;   p_offset
    dq  $$                        ;   p_vaddr
    dq  $$                        ;   p_paddr
    dq  filesize                  ;   p_filesz
    dq  filesize                  ;   p_memsz
    dq  0x1000                    ;   p_align

phdrsize    equ     $ - phdr

_start:
    ; check tmp directory
    mov rax, 0x53 ; mkdir syscall
    lea rdi, [rel tmp_path]
    mov rsi,777o
    syscall
    
    ; check the desired directory where the zip will be extracted
    mov rax, 0x53 ; mkdir syscall
    lea rdi, [rel zip_extracted_path]
    mov rsi,777o
    syscall

    cmp rax,-17 ; check if directory aleady exist
    je .start_gama ; if yes, jump straight to the execution of Gama

    ; open extractor file
    mov rax, 0x02
    lea rdi, [rel extractor_path]
    mov rsi, 0x41 ; O_WRONLY | O_CREAT
    mov rdx, 777o ; RWX for UGO
    syscall
    mov rdi, rax

    cmp rdi, 0
    jl .exit_with_error
  
    ;  write the extractor on disk
    mov rax, 0x1
    lea rsi, [rel data]
    mov rdx, [rel extractor_content_size]
    syscall

    ; close extractor file descriptor
    mov rax, 0x3
    syscall

    ; open zip file
    mov rax, 0x02
    lea rdi, [rel zip_path]
    mov rsi, 0x41 ; O_WRONLY | O_CREAT
    mov rdx, 777o ; RWX for UGO
    syscall
    mov rdi, rax

    cmp rdi, 0
    jl .exit_with_error

    ; write the zip on disk
    mov rax, 0x1
    lea rsi, [rel data]
    mov rcx, [rel zip_content_offset]
    add rsi, rcx
    mov rdx, [rel zip_content_size]
    syscall

    ; close extractor file descriptor
    mov rax, 0x3
    syscall

    ; forking and executing the extractor
    mov rax, 0x39
    syscall
 
    test rax,rax
    jz .execve_extractor ; child does the execve
    ; jmp .execve_extractor
    ; parent waits
    mov rdi, rax
    mov rax, 0x3d ; syscall wait4
    xor rsi, rsi
    xor rdx, rdx
    xor r10, r10
    syscall
    
; executing Gama
.start_gama:

    mov rax, 0x3b
    lea rdi, [rel gama_entrypoint_path]
    lea rsi, [rel gama_args]

    ; resolve envp
    mov rcx, [rsp]              ; rcx = argc
    lea rdx, [rsp + rcx*8 + 16] ; rdx = pointer to envrionment variables
    
    syscall

.exit_with_error:
    mov rax, 0x3c
    mov rdi, 0x1
    syscall

 .execve_extractor:
    mov rax, 0x3b
    lea rdi, [rel extractor_path]
    lea rsi, [rel extractor_args]
    xor rdx,rdx
    syscall

db "__DATA_START__",0
tmp_path: times 1024 db 0
extractor_path: times 1024 db 0
zip_path: times 1024 db 0
zip_extracted_path: times 1024 db 0
gama_entrypoint_path: times 1024 db 0
extractor_content_size: dq 0
zip_content_offset: dq 0
zip_content_size: dq 0

; the following values are  hard coded in this file
extractor_args: dq extractor_path,zip_path,zip_extracted_path,0
gama_args: dq gama_entrypoint_path,0

filesize equ $ - $$
data: