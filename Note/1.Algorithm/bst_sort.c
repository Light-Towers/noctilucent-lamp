#include <stdio.h>
#include <stdlib.h>
#include <stdbool.h>	// 包含布尔类型头文件

typedef struct BSTNode {
    int data;
    struct BSTNode *lchild;  // 左孩子
    struct BSTNode *rchild;  // 右孩子
} BSTNode, *BSTree;

// 查找成功时，p指向值为key的节点。如果查找失败，则p指向遍历的最后一个节点
bool Search(BSTree bst, int key, BSTree f, BSTree *p) {
    if (!bst) {
        *p = f;
        return false;
    }
    if (bst->data == key) {  // 查找成功，直接返回
        *p = bst;
        return true;
    } else if (bst->data < key) {
        return Search(bst->rchild, key, bst, p);
    }
    return Search(bst->lchild, key, bst, p);
}

// 中序递归遍历二叉树
void InOrderTraverse(BSTree bst) {
    if (bst != NULL) {
        InOrderTraverse(bst->lchild);
        printf("%d ", bst->data);
        InOrderTraverse(bst->rchild);
    }
}

// 生成一个节点并进行初始化
static BSTNode* CreateNode(int data) {
    BSTNode *pTmp = (BSTNode*)malloc(sizeof(BSTNode));
    if (pTmp == NULL) {
        fprintf(stderr, "Memory allocation failed\n");
        return NULL;
    }
    pTmp->data = data;
    pTmp->lchild = NULL;
    pTmp->rchild = NULL;
    return pTmp;
}

// 插入节点
bool Insert(BSTree *bst, int key) {
    if (*bst == NULL) {  // 空树
        *bst = CreateNode(key);  // 插入根节点
        if (*bst == NULL) {
            return false;
        }
        return true;
    }

    BSTNode *p;
    // 先在二叉排序树中查找要插入的值是否已经存在
    if (!Search(*bst, key, NULL, &p)) {  // 如果查找失败，则插入；此时p指向遍历的最后一个节点
        BSTNode *pNew = CreateNode(key);
        if (pNew == NULL) {
            return false;
        }
        if (key < p->data) {  // 将新节点作为p的左孩子
            p->lchild = pNew;
        } else if (key > p->data) {  // 将新节点作为p的右孩子
            p->rchild = pNew;
        }
        return true;  // 插入成功
    } else {
        printf("\nThe node(%d) already exists.\n", key);  // 存在
    }
    return false;
}

// 释放二叉树内存
void FreeTree(BSTree bst) {
    if (bst != NULL) {
        FreeTree(bst->lchild);
        FreeTree(bst->rchild);
        free(bst);
    }
}

int main(void) {
    BSTNode *root = NULL;
    int num, i, a;

    printf("请输入排序的个数：");
    if (scanf("%d", &num) != 1) {
        fprintf(stderr, "Invalid input for number of elements\n");
        return 1;
    }

    printf("请输入排序数：");
    for (i = 0; i < num; i++) {
        if (scanf("%d", &a) != 1) {
            fprintf(stderr, "Invalid input for element %d\n", i + 1);
            FreeTree(root);
            return 1;
        }
        if (!Insert(&root, a)) {
            fprintf(stderr, "Failed to insert element %d\n", a);
            FreeTree(root);
            return 1;
        }
    }

    printf("排序后的序列：");
    InOrderTraverse(root);  // 中序输出
    FreeTree(root);
    return 0;
}