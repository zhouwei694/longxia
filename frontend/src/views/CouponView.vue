<template>
  <div class="coupon-view">
    <!-- 搜索区域 -->
    <a-card class="search-card" :bordered="false">
      <a-form layout="inline" :model="searchForm" class="search-form">
        <a-form-item label="卡券号">
          <a-input
            v-model:value="searchForm.couponNo"
            placeholder="请输入卡券号"
            allow-clear
            style="width: 160px"
          />
        </a-form-item>
        <a-form-item label="卡券名称">
          <a-input
            v-model:value="searchForm.couponName"
            placeholder="请输入卡券名称"
            allow-clear
            style="width: 160px"
          />
        </a-form-item>
        <a-form-item label="核销人">
          <a-input
            v-model:value="searchForm.verifyBy"
            placeholder="请输入核销人"
            allow-clear
            style="width: 160px"
          />
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="handleSearch">
              <template #icon><SearchOutlined /></template>
              查询
            </a-button>
            <a-button @click="handleReset">重置</a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>

    <!-- 操作栏 -->
    <div class="action-bar">
      <a-space>
        <a-button v-if="isAdmin" type="primary" @click="generateModal.open = true">
          <template #icon><PlusOutlined /></template>
          生成卡券
        </a-button>
        <a-button
          v-if="activeTab === 0"
          type="primary"
          ghost
          :disabled="!selectedRowKeys.length"
          @click="handleOpenActivate"
        >
          <template #icon><CheckCircleOutlined /></template>
          批量激活 {{ selectedRowKeys.length ? `(${selectedRowKeys.length})` : '' }}
        </a-button>
        <a-button
          v-if="activeTab === 0 && isAdmin"
          :disabled="!selectedRowKeys.length"
          @click="handleOpenBatchEdit"
        >
          <template #icon><EditOutlined /></template>
          批量修改 {{ selectedRowKeys.length ? `(${selectedRowKeys.length})` : '' }}
        </a-button>
        <a-button
          v-if="activeTab === 0 && isAdmin"
          danger
          :disabled="!selectedRowKeys.length"
          @click="handleBatchDelete"
        >
          <template #icon><DeleteOutlined /></template>
          批量删除 {{ selectedRowKeys.length ? `(${selectedRowKeys.length})` : '' }}
        </a-button>
        <a-button v-if="activeTab === 1" type="primary" danger @click="verifyModal.open = true">
          <template #icon><SafetyCertificateOutlined /></template>
          核销卡券
        </a-button>
        <a-button v-if="isAdmin" :loading="exportLoading" @click="handleExport">
          <template #icon><DownloadOutlined /></template>
          导出
        </a-button>
      </a-space>
      <span class="data-info">共 {{ pagination.total }} 条数据</span>
    </div>

    <!-- 状态页签 + 数据表格 -->
    <a-card :bordered="false" class="table-card">
      <a-tabs v-model:activeKey="activeTab" @change="handleTabChange">
        <a-tab-pane v-if="isAdmin" :key="0" tab="未激活" />
        <a-tab-pane :key="1" tab="已激活" />
        <a-tab-pane :key="2" tab="已核销" />
      </a-tabs>
      <a-table
        :columns="columns"
        :data-source="couponList"
        :pagination="tablePagination"
        :loading="loading"
        :row-selection="activeTab === 0 ? { selectedRowKeys, onChange: onSelectChange } : undefined"
        row-key="id"
        :scroll="{ x: 1300 }"
        @change="handleTableChange"
        size="middle"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.dataIndex === 'couponUrl'">
            <a-image
              v-if="record.couponUrl"
              :src="getImageUrl(record.couponUrl)"
              :width="60"
              :height="60"
              style="object-fit: cover; border-radius: 4px"
              :preview="{ src: getImageUrl(record.couponUrl) }"
            />
            <span v-else>-</span>
          </template>
          <template v-if="column.dataIndex === 'displayAmount' || column.dataIndex === 'actualAmount'">
            ¥{{ record[column.dataIndex]?.toFixed(2) ?? '-' }}
          </template>
          <template v-if="column.dataIndex === 'createTime' || column.dataIndex === 'verifyTime'">
            {{ formatTime(record[column.dataIndex]) }}
          </template>
          <template v-if="column.dataIndex === 'action' && activeTab === 0">
            <a-space>
              <a-button type="link" size="small" @click="handleOpenEdit(record)">
                <template #icon><EditOutlined /></template>
                修改
              </a-button>
              <a-button type="link" size="small" @click="handleActivateSingle(record)">
                <template #icon><PoweroffOutlined /></template>
                激活
              </a-button>
              <a-button type="link" size="small" danger @click="handleDelete(record)">
                <template #icon><DeleteOutlined /></template>
                删除
              </a-button>
            </a-space>
          </template>
          <template v-if="column.dataIndex === 'action' && activeTab === 2">
            <a-button type="link" size="small" @click="handleOpenVerifiedEdit(record)">
              <template #icon><EditOutlined /></template>
              修改
            </a-button>
          </template>
        </template>
      </a-table>
    </a-card>

    <!-- 生成卡券弹窗 -->
    <a-modal
      v-model:open="generateModal.open"
      title="生成卡券"
      @ok="handleGenerate"
      :confirm-loading="generateModal.loading"
      :mask-closable="false"
    >
      <a-form
        :model="generateModal.form"
        :label-col="{ span: 6 }"
        :wrapper-col="{ span: 16 }"
        style="margin-top: 16px"
      >
        <a-form-item label="卡券名称" required>
          <a-input v-model:value="generateModal.form.couponName" placeholder="请输入卡券名称" />
        </a-form-item>
        <a-form-item label="展示金额" required>
          <a-input-number
            v-model:value="generateModal.form.displayAmount"
            :min="0"
            :precision="2"
            placeholder="请输入展示金额"
            style="width: 100%"
            :prefix="'¥'"
          />
        </a-form-item>
        <a-form-item label="实际金额" required>
          <a-input-number
            v-model:value="generateModal.form.actualAmount"
            :min="0"
            :precision="2"
            placeholder="请输入实际金额"
            style="width: 100%"
            :prefix="'¥'"
          />
        </a-form-item>
        <a-form-item label="卡券图片">
          <a-upload
            :before-upload="handleBeforeUpload"
            :file-list="generateModal.fileList"
            :max-count="1"
            accept="image/*"
            list-type="picture-card"
            @remove="handleRemoveUpload"
          >
            <div v-if="!generateModal.fileList.length">
              <UploadOutlined />
              <div style="margin-top: 8px">上传图片</div>
            </div>
          </a-upload>
        </a-form-item>
        <a-form-item label="生成数量" required>
          <a-input-number
            v-model:value="generateModal.form.count"
            :min="1"
            :max="200"
            :precision="0"
            placeholder="请输入生成数量（1~200）"
            style="width: 100%"
          />
        </a-form-item>
      </a-form>
      <a-alert :message="`将一次性生成 ${generateModal.form.count || 50} 张卡券，卡券号自动递增，密码随机生成`" type="info" show-icon />
    </a-modal>

    <!-- 激活卡券弹窗 -->
    <a-modal
      v-model:open="activateModal.open"
      title="批量激活卡券"
      @ok="handleActivate"
      :confirm-loading="activateModal.loading"
      :mask-closable="false"
    >
      <a-alert
        :message="`已选中 ${selectedRowKeys.length} 张卡券，以下字段为选填，留空则不修改`"
        type="info"
        show-icon
        style="margin-bottom: 16px"
      />
      <a-form
        :model="activateModal.form"
        :label-col="{ span: 6 }"
        :wrapper-col="{ span: 16 }"
      >
        <a-form-item label="卡券名称">
          <a-input v-model:value="activateModal.form.couponName" placeholder="留空则不修改" allow-clear />
        </a-form-item>
        <a-form-item label="展示金额">
          <a-input-number
            v-model:value="activateModal.form.displayAmount"
            :min="0"
            :precision="2"
            placeholder="留空则不修改"
            style="width: 100%"
          />
        </a-form-item>
        <a-form-item label="实际金额">
          <a-input-number
            v-model:value="activateModal.form.actualAmount"
            :min="0"
            :precision="2"
            placeholder="留空则不修改"
            style="width: 100%"
          />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 核销卡券弹窗 -->
    <a-modal
      v-model:open="verifyModal.open"
      title="核销卡券"
      @ok="handleVerify"
      :confirm-loading="verifyModal.loading"
      :mask-closable="false"
    >
      <a-form
        :model="verifyModal.form"
        :label-col="{ span: 6 }"
        :wrapper-col="{ span: 16 }"
        style="margin-top: 16px"
      >
        <a-form-item label="卡券密码" required>
          <a-input
            v-model:value="verifyModal.form.couponPassword"
            placeholder="请输入卡券密码"
            allow-clear
          />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 修改卡券弹窗 -->
    <a-modal
      v-model:open="editModal.open"
      title="修改卡券"
      @ok="handleEdit"
      :confirm-loading="editModal.loading"
      :mask-closable="false"
    >
      <a-form
        :model="editModal.form"
        :label-col="{ span: 6 }"
        :wrapper-col="{ span: 16 }"
        style="margin-top: 16px"
      >
        <a-form-item label="卡券名称" required>
          <a-input v-model:value="editModal.form.couponName" placeholder="请输入卡券名称" />
        </a-form-item>
        <a-form-item label="展示金额" required>
          <a-input-number
            v-model:value="editModal.form.displayAmount"
            :min="0"
            :precision="2"
            placeholder="请输入展示金额"
            style="width: 100%"
            :prefix="'¥'"
          />
        </a-form-item>
        <a-form-item label="实际金额" required>
          <a-input-number
            v-model:value="editModal.form.actualAmount"
            :min="0"
            :precision="2"
            placeholder="请输入实际金额"
            style="width: 100%"
            :prefix="'¥'"
          />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 已核销卡券修改弹窗 -->
    <a-modal
      v-model:open="verifiedEditModal.open"
      title="修改收件信息"
      @ok="handleVerifiedEdit"
      :confirm-loading="verifiedEditModal.loading"
      :mask-closable="false"
    >
      <a-form
        :model="verifiedEditModal.form"
        :label-col="{ span: 7 }"
        :wrapper-col="{ span: 15 }"
        style="margin-top: 16px"
      >
        <a-form-item label="收件人电话">
          <a-input v-model:value="verifiedEditModal.form.recipientPhone" placeholder="请输入收件人电话" allow-clear />
        </a-form-item>
        <a-form-item label="收件人快递号">
          <a-input v-model:value="verifiedEditModal.form.recipientExpressNo" placeholder="请输入收件人快递号" allow-clear />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 批量修改卡券弹窗 -->
    <a-modal
      v-model:open="batchEditModal.open"
      title="批量修改卡券"
      @ok="handleBatchEdit"
      :confirm-loading="batchEditModal.loading"
      :mask-closable="false"
    >
      <a-alert
        :message="`已选中 ${selectedRowKeys.length} 张卡券，以下字段为选填，留空则不修改`"
        type="info"
        show-icon
        style="margin-bottom: 16px"
      />
      <a-form
        :model="batchEditModal.form"
        :label-col="{ span: 6 }"
        :wrapper-col="{ span: 16 }"
      >
        <a-form-item label="卡券名称">
          <a-input v-model:value="batchEditModal.form.couponName" placeholder="留空则不修改" allow-clear />
        </a-form-item>
        <a-form-item label="展示金额">
          <a-input-number
            v-model:value="batchEditModal.form.displayAmount"
            :min="0"
            :precision="2"
            placeholder="留空则不修改"
            style="width: 100%"
          />
        </a-form-item>
        <a-form-item label="实际金额">
          <a-input-number
            v-model:value="batchEditModal.form.actualAmount"
            :min="0"
            :precision="2"
            placeholder="留空则不修改"
            style="width: 100%"
          />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { message, Modal } from 'ant-design-vue'
import {
  SearchOutlined,
  PlusOutlined,
  CheckCircleOutlined,
  SafetyCertificateOutlined,
  DownloadOutlined,
  EditOutlined,
  PoweroffOutlined,
  DeleteOutlined,
  UploadOutlined,
} from '@ant-design/icons-vue'
import { listCouponByPage, generateCoupons, activateCoupons, verifyCoupon, exportCoupons, updateCoupon, activateSingleCoupon, deleteCoupon, updateVerifiedCoupon, batchUpdateCoupon, batchDeleteCoupon, uploadCouponImage } from '../api/coupon'
import type { CouponVO } from '../types'
import { useUserStore } from '../stores/user'

const userStore = useUserStore()
const isAdmin = computed(() => userStore.loginUser?.userRole === 'admin')

const columns = computed(() => {
  const base = [
    { title: 'ID', dataIndex: 'id', width: 70, fixed: 'left' as const },
    { title: '卡券图片', dataIndex: 'couponUrl', width: 100 },
    { title: '卡券名称', dataIndex: 'couponName', width: 130 },
    { title: '卡券号', dataIndex: 'couponNo', width: 90 },
    { title: '卡券密码', dataIndex: 'couponPassword', width: 120 },
    { title: '展示金额', dataIndex: 'displayAmount', width: 100, align: 'right' as const },
    { title: '实际金额', dataIndex: 'actualAmount', width: 100, align: 'right' as const },
    { title: '创建人', dataIndex: 'createBy', width: 100 },
    { title: '核销人', dataIndex: 'verifyBy', width: 100 },
    { title: '创建时间', dataIndex: 'createTime', width: 170 },
    { title: '核销时间', dataIndex: 'verifyTime', width: 170 },
  ]
  if (activeTab.value === 2) {
    base.push(
      { title: '收件人电话', dataIndex: 'recipientPhone', width: 130 } as any,
      { title: '收件人快递号', dataIndex: 'recipientExpressNo', width: 150 } as any,
      { title: '操作', dataIndex: 'action', width: 100, fixed: 'right' as const } as any,
    )
  }
  if (activeTab.value === 0 && isAdmin.value) {
    base.push({ title: '操作', dataIndex: 'action', width: 200, fixed: 'right' as const } as any)
  }
  return base
})

const route = useRoute()

// 当前页签状态，支持从路由 query 初始化
const activeTab = ref<number>(route.query.tab != null ? Number(route.query.tab) : 0)

// 搜索（不再包含 status）
const searchForm = reactive({
  couponNo: '',
  couponName: '',
  verifyBy: '',
})

// 表格数据
const couponList = ref<CouponVO[]>([])
const loading = ref(false)
const exportLoading = ref(false)
const selectedRowKeys = ref<number[]>([])
const pagination = reactive({ current: 1, pageSize: 10, total: 0 })

const tablePagination = computed(() => ({
  current: pagination.current,
  pageSize: pagination.pageSize,
  total: pagination.total,
  showSizeChanger: true,
  showTotal: (total: number) => `共 ${total} 条`,
}))

const onSelectChange = (keys: number[]) => {
  selectedRowKeys.value = keys
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await listCouponByPage({
      pageNum: pagination.current,
      pageSize: pagination.pageSize,
      status: activeTab.value,
      couponNo: searchForm.couponNo || undefined,
      couponName: searchForm.couponName || undefined,
      verifyBy: searchForm.verifyBy || undefined,
    })
    if (res.code === 0) {
      couponList.value = res.data.records
      pagination.total = Number(res.data.totalRow)
    } else {
      message.error(res.message || '查询失败')
    }
  } catch {
    message.error('查询请求失败')
  } finally {
    loading.value = false
  }
}

const handleTabChange = () => {
  pagination.current = 1
  selectedRowKeys.value = []
  loadData()
}

const handleTableChange = (pag: any) => {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  loadData()
}

const handleSearch = () => {
  pagination.current = 1
  selectedRowKeys.value = []
  loadData()
}

const handleReset = () => {
  Object.assign(searchForm, { couponNo: '', couponName: '', verifyBy: '' })
  handleSearch()
}

const formatTime = (time: string) => {
  if (!time) return '-'
  return time.replace('T', ' ')
}

const getImageUrl = (url: string) => {
  if (!url) return ''
  // 兼容旧的Base64数据和新的URL路径
  if (url.startsWith('data:') || url.startsWith('http')) return url
  return '/api' + url
}

const handleExport = async () => {
  exportLoading.value = true
  try {
    const blob = await exportCoupons({
      pageNum: pagination.current,
      pageSize: pagination.pageSize,
      status: activeTab.value,
      couponNo: searchForm.couponNo || undefined,
      couponName: searchForm.couponName || undefined,
      verifyBy: searchForm.verifyBy || undefined,
    })
    const downloadUrl = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = downloadUrl
    link.download = `卡券列表_${activeTab.value}.xlsx`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(downloadUrl)
    message.success('导出成功')
  } catch {
    message.error('导出失败')
  } finally {
    exportLoading.value = false
  }
}

// ==================== 生成卡券 ====================
const generateModal = reactive({
  open: false,
  loading: false,
  form: { couponName: '', displayAmount: undefined as number | undefined, actualAmount: undefined as number | undefined, count: 50 as number },
  fileList: [] as any[],
  couponUrl: '' as string,
})

const handleBeforeUpload = async (file: File) => {
  try {
    const res = await uploadCouponImage(file)
    if (res.code === 0) {
      generateModal.couponUrl = res.data
      generateModal.fileList = [{ uid: '-1', name: file.name, status: 'done', url: '/api' + res.data }]
    } else {
      message.error(res.message || '图片上传失败')
    }
  } catch {
    message.error('图片上传请求失败')
  }
  return false
}

const handleRemoveUpload = () => {
  generateModal.fileList = []
  generateModal.couponUrl = ''
}

const handleGenerate = async () => {
  const { couponName, displayAmount, actualAmount, count } = generateModal.form
  if (!couponName) return message.warning('请输入卡券名称')
  if (displayAmount == null) return message.warning('请输入展示金额')
  if (actualAmount == null) return message.warning('请输入实际金额')
  if (count == null || count < 1 || count > 200) return message.warning('生成数量必须在1~200之间')

  generateModal.loading = true
  try {
    const res = await generateCoupons({
      couponName,
      displayAmount,
      actualAmount,
      couponUrl: generateModal.couponUrl || undefined,
      count,
    })
    if (res.code === 0) {
      message.success(`成功生成 ${count} 张卡券`)
      generateModal.open = false
      generateModal.form = { couponName: '', displayAmount: undefined, actualAmount: undefined, count: 50 }
      generateModal.fileList = []
      generateModal.couponUrl = ''
      loadData()
    } else {
      message.error(res.message || '生成失败')
    }
  } catch {
    message.error('生成请求失败')
  } finally {
    generateModal.loading = false
  }
}

// ==================== 激活卡券 ====================
const activateModal = reactive({
  open: false,
  loading: false,
  form: { couponName: '', displayAmount: undefined as number | undefined, actualAmount: undefined as number | undefined },
})

const handleOpenActivate = () => {
  activateModal.form = { couponName: '', displayAmount: undefined, actualAmount: undefined }
  activateModal.open = true
}

const handleActivate = async () => {
  if (!selectedRowKeys.value.length) return message.warning('请选择要激活的卡券')

  activateModal.loading = true
  try {
    const res = await activateCoupons({
      ids: selectedRowKeys.value,
      couponName: activateModal.form.couponName || undefined,
      displayAmount: activateModal.form.displayAmount,
      actualAmount: activateModal.form.actualAmount,
    })
    if (res.code === 0) {
      message.success(`成功激活 ${selectedRowKeys.value.length} 张卡券`)
      activateModal.open = false
      selectedRowKeys.value = []
      loadData()
    } else {
      message.error(res.message || '激活失败')
    }
  } catch {
    message.error('激活请求失败')
  } finally {
    activateModal.loading = false
  }
}

// ==================== 核销卡券 ====================
const verifyModal = reactive({
  open: false,
  loading: false,
  form: { couponPassword: '' },
})

const handleVerify = async () => {
  if (!verifyModal.form.couponPassword) return message.warning('请输入卡券密码')

  verifyModal.loading = true
  try {
    const res = await verifyCoupon({ couponPassword: verifyModal.form.couponPassword })
    if (res.code === 0) {
      message.success('核销成功')
      verifyModal.open = false
      verifyModal.form.couponPassword = ''
      loadData()
    } else {
      message.error(res.message || '核销失败')
    }
  } catch {
    message.error('核销请求失败')
  } finally {
    verifyModal.loading = false
  }
}

// ==================== 修改卡券 ====================
const editModal = reactive({
  open: false,
  loading: false,
  form: { id: 0, couponName: '', displayAmount: undefined as number | undefined, actualAmount: undefined as number | undefined },
})

const handleOpenEdit = (record: CouponVO) => {
  editModal.form = {
    id: record.id,
    couponName: record.couponName,
    displayAmount: record.displayAmount,
    actualAmount: record.actualAmount,
  }
  editModal.open = true
}

const handleEdit = async () => {
  const { id, couponName, displayAmount, actualAmount } = editModal.form
  if (!couponName) return message.warning('请输入卡券名称')
  if (displayAmount == null) return message.warning('请输入展示金额')
  if (actualAmount == null) return message.warning('请输入实际金额')

  editModal.loading = true
  try {
    const res = await updateCoupon({ id, couponName, displayAmount, actualAmount })
    if (res.code === 0) {
      message.success('修改成功')
      editModal.open = false
      loadData()
    } else {
      message.error(res.message || '修改失败')
    }
  } catch {
    message.error('修改请求失败')
  } finally {
    editModal.loading = false
  }
}

// ==================== 单个激活卡券 ====================
const handleActivateSingle = (record: CouponVO) => {
  Modal.confirm({
    title: '确认激活',
    content: `确定要激活卡券 ${record.couponNo} 吗？`,
    okText: '确定',
    cancelText: '取消',
    onOk: async () => {
      try {
        const res = await activateSingleCoupon(record.id)
        if (res.code === 0) {
          message.success('激活成功')
          loadData()
        } else {
          message.error(res.message || '激活失败')
        }
      } catch {
        message.error('激活请求失败')
      }
    },
  })
}

// ==================== 删除卡券 ====================
const handleDelete = (record: CouponVO) => {
  Modal.confirm({
    title: '确认删除',
    content: `确定要删除卡券 ${record.couponNo} 吗？删除后不可恢复。`,
    okText: '确定',
    cancelText: '取消',
    okButtonProps: { danger: true },
    onOk: async () => {
      try {
        const res = await deleteCoupon(record.id)
        if (res.code === 0) {
          message.success('删除成功')
          loadData()
        } else {
          message.error(res.message || '删除失败')
        }
      } catch {
        message.error('删除请求失败')
      }
    },
  })
}

// ==================== 已核销卡券修改 ====================
const verifiedEditModal = reactive({
  open: false,
  loading: false,
  form: { id: 0, recipientPhone: '', recipientExpressNo: '' },
})

const handleOpenVerifiedEdit = (record: CouponVO) => {
  verifiedEditModal.form = {
    id: record.id,
    recipientPhone: record.recipientPhone || '',
    recipientExpressNo: record.recipientExpressNo || '',
  }
  verifiedEditModal.open = true
}

const handleVerifiedEdit = async () => {
  verifiedEditModal.loading = true
  try {
    const res = await updateVerifiedCoupon({
      id: verifiedEditModal.form.id,
      recipientPhone: verifiedEditModal.form.recipientPhone || undefined,
      recipientExpressNo: verifiedEditModal.form.recipientExpressNo || undefined,
    })
    if (res.code === 0) {
      message.success('修改成功')
      verifiedEditModal.open = false
      loadData()
    } else {
      message.error(res.message || '修改失败')
    }
  } catch {
    message.error('修改请求失败')
  } finally {
    verifiedEditModal.loading = false
  }
}

// ==================== 批量修改卡券 ====================
const batchEditModal = reactive({
  open: false,
  loading: false,
  form: { couponName: '', displayAmount: undefined as number | undefined, actualAmount: undefined as number | undefined },
})

const handleOpenBatchEdit = () => {
  batchEditModal.form = { couponName: '', displayAmount: undefined, actualAmount: undefined }
  batchEditModal.open = true
}

const handleBatchEdit = async () => {
  if (!selectedRowKeys.value.length) return message.warning('请选择要修改的卡券')

  batchEditModal.loading = true
  try {
    const res = await batchUpdateCoupon({
      ids: selectedRowKeys.value,
      couponName: batchEditModal.form.couponName || undefined,
      displayAmount: batchEditModal.form.displayAmount,
      actualAmount: batchEditModal.form.actualAmount,
    })
    if (res.code === 0) {
      message.success(`成功修改 ${selectedRowKeys.value.length} 张卡券`)
      batchEditModal.open = false
      selectedRowKeys.value = []
      loadData()
    } else {
      message.error(res.message || '修改失败')
    }
  } catch {
    message.error('修改请求失败')
  } finally {
    batchEditModal.loading = false
  }
}

// ==================== 批量删除卡券 ====================
const handleBatchDelete = () => {
  if (!selectedRowKeys.value.length) return message.warning('请选择要删除的卡券')
  Modal.confirm({
    title: '确认批量删除',
    content: `确定要删除选中的 ${selectedRowKeys.value.length} 张卡券吗？删除后不可恢复。`,
    okText: '确定',
    cancelText: '取消',
    okButtonProps: { danger: true },
    onOk: async () => {
      try {
        const res = await batchDeleteCoupon({ ids: selectedRowKeys.value })
        if (res.code === 0) {
          message.success(`成功删除 ${selectedRowKeys.value.length} 张卡券`)
          selectedRowKeys.value = []
          loadData()
        } else {
          message.error(res.message || '删除失败')
        }
      } catch {
        message.error('删除请求失败')
      }
    },
  })
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.coupon-view {
  max-width: 1400px;
  margin: 0 auto;
}

.search-card {
  margin-bottom: 16px;
  border-radius: 8px;
}

.search-form {
  display: flex;
  flex-wrap: wrap;
  gap: 8px 0;
}

.action-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.data-info {
  font-size: 13px;
  color: #999;
}

.table-card {
  border-radius: 8px;
}

.table-card :deep(.ant-tabs) {
  margin-bottom: 0;
}

.table-card :deep(.ant-tabs-nav) {
  margin-bottom: 16px;
}
</style>
