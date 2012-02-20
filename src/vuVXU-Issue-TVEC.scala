package hwacha

import Chisel._
import Node._
import Config._
import Commands._
import Interface._

class vuVXU_Issue_TVEC extends Component
{
  val io = new io_vxu_issue_tvec()

  val ISSUE_TVEC = Bits(0,1)
  val ISSUE_VT = Bits(1,1)

  val next_state = Wire(){Bits(width = 1)}
  val reg_state = Reg(next_state, resetVal = ISSUE_TVEC)

  val tvec_active = (reg_state === ISSUE_TVEC)
  io.active := tvec_active    


//-------------------------------------------------------------------------\\
// DECODE                                                                  \\
//-------------------------------------------------------------------------\\

  val cmd = io.vxu_cmdq.bits(XCMD_CMCODE)
  val vd = io.vxu_cmdq.bits(XCMD_VD)
  val vt = io.vxu_cmdq.bits(XCMD_VS)
  val imm = io.vxu_immq.bits
  val imm2 = io.vxu_imm2q.bits

  val n = Bool(false)
  val y = Bool(true)

  val cs =
  ListLookup(cmd,
                     //                                                                decode_fence_cv
                     //                                                                | decode_fence_v
                     //                                                                | | vd_valid
                     //                                                                | | | decode_vcfg
                     //                                                                | | | | decode_setvl
                     //                                                                | | | | | decode_vf
                     //                                                                | | | | | | deq_vxu_immq
                     //         val            dhazard       shazard       bhazard msrc| | | | | | | deq_vxu_imm2q  
                     //         |              |             |             |        |  | | | | | | | | stride  mem_type_float
                     //         |              |             |             |        |  | | | | | | | | |         |   mem_type mem_cmd
                     //         |              |             |             |        |  | | | | | | | | |         |     |      |
                     List(Bits("b000",3),Bits("b00",2),Bits("b00",2),Bits("b000",3),M0,n,n,n,n,n,n,n,n,Bits(0,4),MTF_X,MT_X,M_X),Array(
    CMD_VVCFGIVL->   List(Bits("b000",3),Bits("b00",2),Bits("b00",2),Bits("b000",3),M0,n,n,n,y,y,n,y,n,Bits(0,4),MTF_X,MT_X,M_X),
    CMD_VSETVL->     List(Bits("b000",3),Bits("b00",2),Bits("b00",2),Bits("b000",3),M0,n,n,n,n,y,n,y,n,Bits(0,4),MTF_X,MT_X,M_X),
    CMD_VF->         List(Bits("b000",3),Bits("b00",2),Bits("b00",2),Bits("b000",3),M0,n,n,n,n,n,y,y,n,Bits(0,4),MTF_X,MT_X,M_X),
                                                                                                                 
    CMD_FENCE_L_V->  List(Bits("b000",3),Bits("b00",2),Bits("b00",2),Bits("b000",3),M0,n,y,n,n,n,n,n,n,Bits(0,4),MTF_X,MT_X,M_X),
    CMD_FENCE_G_V->  List(Bits("b000",3),Bits("b00",2),Bits("b00",2),Bits("b000",3),M0,n,y,n,n,n,n,n,n,Bits(0,4),MTF_X,MT_X,M_X),
    CMD_FENCE_L_CV-> List(Bits("b000",3),Bits("b00",2),Bits("b00",2),Bits("b000",3),M0,y,n,n,n,n,n,n,n,Bits(0,4),MTF_X,MT_X,M_X),
    CMD_FENCE_G_CV-> List(Bits("b000",3),Bits("b00",2),Bits("b00",2),Bits("b000",3),M0,y,n,n,n,n,n,n,n,Bits(0,4),MTF_X,MT_X,M_X),
                                                                                                                 
    CMD_VMVV->       List(Bits("b001",3),Bits("b11",2),Bits("b00",2),Bits("b001",3),MR,n,n,y,n,n,n,n,n,Bits(0,4),MTF_X,MT_X,M_X),
    CMD_VMSV->       List(Bits("b001",3),Bits("b10",2),Bits("b00",2),Bits("b001",3),MI,n,n,y,n,n,n,y,n,Bits(0,4),MTF_X,MT_X,M_X),
    CMD_VFMVV->      List(Bits("b001",3),Bits("b11",2),Bits("b00",2),Bits("b001",3),MR,n,n,y,n,n,n,n,n,Bits(0,4),MTF_X,MT_X,M_X),
                                                                                                                 
    CMD_VLD       -> List(Bits("b010",3),Bits("b10",2),Bits("b01",2),Bits("b010",3),M0,n,n,y,n,n,n,y,n,Bits(8,4),MTF_X,MT_D,M_XRD),
    CMD_VLW       -> List(Bits("b010",3),Bits("b10",2),Bits("b01",2),Bits("b010",3),M0,n,n,y,n,n,n,y,n,Bits(4,4),MTF_X,MT_W,M_XRD),
    CMD_VLWU      -> List(Bits("b010",3),Bits("b10",2),Bits("b01",2),Bits("b010",3),M0,n,n,y,n,n,n,y,n,Bits(4,4),MTF_X,MT_WU,M_XRD),
    CMD_VLH       -> List(Bits("b010",3),Bits("b10",2),Bits("b01",2),Bits("b010",3),M0,n,n,y,n,n,n,y,n,Bits(2,4),MTF_X,MT_H,M_XRD),
    CMD_VLHU      -> List(Bits("b010",3),Bits("b10",2),Bits("b01",2),Bits("b010",3),M0,n,n,y,n,n,n,y,n,Bits(2,4),MTF_X,MT_HU,M_XRD),
    CMD_VLB       -> List(Bits("b010",3),Bits("b10",2),Bits("b01",2),Bits("b010",3),M0,n,n,y,n,n,n,y,n,Bits(1,4),MTF_X,MT_B,M_XRD),
    CMD_VLBU      -> List(Bits("b010",3),Bits("b10",2),Bits("b01",2),Bits("b010",3),M0,n,n,y,n,n,n,y,n,Bits(1,4),MTF_X,MT_BU,M_XRD),
    CMD_VSD       -> List(Bits("b100",3),Bits("b01",2),Bits("b10",2),Bits("b100",3),M0,n,n,n,n,n,n,y,n,Bits(8,4),MTF_X,MT_D,M_XWR),
    CMD_VSW       -> List(Bits("b100",3),Bits("b01",2),Bits("b10",2),Bits("b100",3),M0,n,n,n,n,n,n,y,n,Bits(4,4),MTF_X,MT_W,M_XWR),
    CMD_VSH       -> List(Bits("b100",3),Bits("b01",2),Bits("b10",2),Bits("b100",3),M0,n,n,n,n,n,n,y,n,Bits(2,4),MTF_X,MT_H,M_XWR),
    CMD_VSB       -> List(Bits("b100",3),Bits("b01",2),Bits("b10",2),Bits("b100",3),M0,n,n,n,n,n,n,y,n,Bits(1,4),MTF_X,MT_B,M_XWR),
                                                                                                                 
    CMD_VFLD      -> List(Bits("b010",3),Bits("b10",2),Bits("b01",2),Bits("b010",3),M0,n,n,y,n,n,n,y,n,Bits(8,4),MTF_Y,MT_D,M_XRD),
    CMD_VFLW      -> List(Bits("b010",3),Bits("b10",2),Bits("b01",2),Bits("b010",3),M0,n,n,y,n,n,n,y,n,Bits(4,4),MTF_Y,MT_W,M_XRD),
    CMD_VFSD      -> List(Bits("b100",3),Bits("b01",2),Bits("b10",2),Bits("b100",3),M0,n,n,n,n,n,n,y,n,Bits(8,4),MTF_Y,MT_D,M_XWR),
    CMD_VFSW      -> List(Bits("b100",3),Bits("b01",2),Bits("b10",2),Bits("b100",3),M0,n,n,n,n,n,n,y,n,Bits(4,4),MTF_Y,MT_W,M_XWR),
                                                                                                                 
    CMD_VLSTD     -> List(Bits("b010",3),Bits("b10",2),Bits("b01",2),Bits("b010",3),M0,n,n,y,n,n,n,y,y,Bits(0,4),MTF_X,MT_D,M_XRD),
    CMD_VLSTW     -> List(Bits("b010",3),Bits("b10",2),Bits("b01",2),Bits("b010",3),M0,n,n,y,n,n,n,y,y,Bits(0,4),MTF_X,MT_W,M_XRD),
    CMD_VLSTWU    -> List(Bits("b010",3),Bits("b10",2),Bits("b01",2),Bits("b010",3),M0,n,n,y,n,n,n,y,y,Bits(0,4),MTF_X,MT_WU,M_XRD),
    CMD_VLSTH     -> List(Bits("b010",3),Bits("b10",2),Bits("b01",2),Bits("b010",3),M0,n,n,y,n,n,n,y,y,Bits(0,4),MTF_X,MT_H,M_XRD),
    CMD_VLSTHU    -> List(Bits("b010",3),Bits("b10",2),Bits("b01",2),Bits("b010",3),M0,n,n,y,n,n,n,y,y,Bits(0,4),MTF_X,MT_HU,M_XRD),
    CMD_VLSTB     -> List(Bits("b010",3),Bits("b10",2),Bits("b01",2),Bits("b010",3),M0,n,n,y,n,n,n,y,y,Bits(0,4),MTF_X,MT_B,M_XRD),
    CMD_VLSTBU    -> List(Bits("b010",3),Bits("b10",2),Bits("b01",2),Bits("b010",3),M0,n,n,y,n,n,n,y,y,Bits(0,4),MTF_X,MT_BU,M_XRD),
    CMD_VSSTD     -> List(Bits("b100",3),Bits("b01",2),Bits("b10",2),Bits("b100",3),M0,n,n,n,n,n,n,y,y,Bits(0,4),MTF_X,MT_D,M_XWR),
    CMD_VSSTW     -> List(Bits("b100",3),Bits("b01",2),Bits("b10",2),Bits("b100",3),M0,n,n,n,n,n,n,y,y,Bits(0,4),MTF_X,MT_W,M_XWR),
    CMD_VSSTH     -> List(Bits("b100",3),Bits("b01",2),Bits("b10",2),Bits("b100",3),M0,n,n,n,n,n,n,y,y,Bits(0,4),MTF_X,MT_H,M_XWR),
    CMD_VSSTB     -> List(Bits("b100",3),Bits("b01",2),Bits("b10",2),Bits("b100",3),M0,n,n,n,n,n,n,y,y,Bits(0,4),MTF_X,MT_B,M_XWR),
                                                                                                                 
    CMD_VFLSTD    -> List(Bits("b010",3),Bits("b10",2),Bits("b01",2),Bits("b010",3),M0,n,n,y,n,n,n,y,y,Bits(0,4),MTF_Y,MT_D,M_XRD),
    CMD_VFLSTW    -> List(Bits("b010",3),Bits("b10",2),Bits("b01",2),Bits("b010",3),M0,n,n,y,n,n,n,y,y,Bits(0,4),MTF_Y,MT_W,M_XRD),
    CMD_VFSSTD    -> List(Bits("b100",3),Bits("b01",2),Bits("b10",2),Bits("b100",3),M0,n,n,n,n,n,n,y,y,Bits(0,4),MTF_Y,MT_D,M_XWR),
    CMD_VFSSTW    -> List(Bits("b100",3),Bits("b01",2),Bits("b10",2),Bits("b100",3),M0,n,n,n,n,n,n,y,y,Bits(0,4),MTF_Y,MT_W,M_XWR)
  ))

  val valid::dhazard::shazard::bhazard::vmsrc::decoded_fence_cv::decoded_fence_v::cs0 = cs
  val vd_valid::decode_vcfg::decode_setvl::decode_vf::deq_vxu_immq::deq_vxu_imm2q::cs1 = cs0
  val addr_stride::mem_type_float::mem_type::mem_cmd::Nil = cs1

  val decode_fence_cv = decoded_fence_cv
  val decode_fence_v = decoded_fence_v

  val fire_vcfg  = tvec_active && io.vxu_cmdq.valid && decode_vcfg
  val fire_setvl = tvec_active && io.vxu_cmdq.valid && decode_setvl
  val fire_vf    = tvec_active && io.vxu_cmdq.valid && decode_vf


//-------------------------------------------------------------------------\\
// REGISTERS                                                               \\
//-------------------------------------------------------------------------\\

  val next_vlen = Wire(){Bits(width = DEF_VLEN)}
  val next_nxregs = Wire(){Bits(width = DEF_REGCNT)}
  val next_nfregs = Wire(){Bits(width = DEF_REGCNT)}
  val next_bactive = Wire(){Bits(width = DEF_BANK)}
  val next_bcnt = Wire(){Bits(width = DEF_BCNT)}
  val next_stride = Wire(){Bits(width = DEF_REGLEN)}

  val reg_vlen = Reg(next_vlen, resetVal = Bits(0,SZ_VLEN))
  val reg_nxregs = Reg(next_nxregs, resetVal = Bits(32,SZ_REGCNT))
  val reg_nfregs = Reg(next_nfregs, resetVal = Bits(32,SZ_REGCNT))
  val reg_bactive = Reg(next_bactive, resetVal = Bits("b1111_1111",SZ_BANK))
  val reg_bcnt = Reg(next_bcnt, resetVal = Bits(8,SZ_LGBANK1))
  val reg_stride = Reg(next_stride, resetVal = Bits(63,SZ_REGLEN))

  next_state := reg_state
  next_vlen := reg_vlen
  next_nxregs := reg_nxregs
  next_nfregs := reg_nfregs
  next_bactive := reg_bactive
  next_bcnt := reg_bcnt
  next_stride := reg_stride

  when (fire_vcfg)
  {
    next_vlen := io.vxu_immq.bits(10,0)
    next_nxregs := io.vxu_immq.bits(16,11)
    next_nfregs := io.vxu_immq.bits(22,17)
    next_bactive := io.vxu_immq.bits(30,23)
    next_bcnt := io.vxu_immq.bits(34,31)
    next_stride := next_nxregs + next_nfregs - Bits(1,2)
  }
  when (fire_setvl)
  {
    next_vlen := io.vxu_immq.bits(10,0)
  }
  when (fire_vf)
  {
    next_state := ISSUE_VT
  }
  when (io.vf.stop)
  {
    next_state := ISSUE_TVEC
  }

//-------------------------------------------------------------------------\\
// SIGNALS                                                                 \\
//-------------------------------------------------------------------------\\

  io.vf.active := (reg_state === ISSUE_VT)
  io.vf.fire := fire_vf
  io.vf.pc := io.vxu_immq.bits(31,0)
  io.vf.nxregs := reg_nxregs
  io.vf.vlen := reg_vlen
  io.vf.imm1_rtag := io.irb_to_issue.imm1_rtag

  io.issue_to_hazard.bcnt := reg_bcnt
  io.issue_to_seq.vlen := reg_vlen
  io.issue_to_seq.stride := reg_stride
  io.issue_to_seq.bcnt := reg_bcnt
  io.issue_to_lane.bactive := reg_bactive

  val mask_vxu_immq_valid = !deq_vxu_immq || io.vxu_immq.valid
  val mask_vxu_imm2q_valid = !deq_vxu_imm2q || io.vxu_imm2q.valid
  val mask_issue_ready = !valid.orR || io.ready

  val mask_irb_imm1b_ready = !deq_vxu_immq || io.irb_imm1b.ready
  val mask_irb_imm2b_ready = !deq_vxu_imm2q || io.irb_imm2b.ready

//-------------------------------------------------------------------------\\
// FENCE LOGIC                                                             \\
//-------------------------------------------------------------------------\\

  val VXU_FORWARD = Bits(0,2)
  val VXU_FENCE_CV = Bits(1,2)
  val VXU_FENCE_V = Bits(2,2)
  
  val state = Reg(resetVal = VXU_FORWARD)

  val fire_fence_cv =
    tvec_active &&
    decode_fence_cv && (state === VXU_FORWARD) && !io.hazard_to_issue.pending_memop &&
    io.vxu_cmdq.valid && mask_vxu_immq_valid && mask_vxu_imm2q_valid && mask_issue_ready

  val fire_fence_v =
    tvec_active &&
    decode_fence_v && (state === VXU_FORWARD) && !io.hazard_to_issue.pending_memop &&
    io.vxu_cmdq.valid && mask_vxu_immq_valid && mask_vxu_imm2q_valid && mask_issue_ready

  io.vec_ackq.bits := Bits(0, 32)
  io.vec_ackq.valid := Bool(false)

  switch (state)
  {
    is (VXU_FORWARD)
    {
      when (fire_fence_cv) {
        state := VXU_FENCE_CV
      }
      when (fire_fence_v) {
        state := VXU_FENCE_V
      }
    }
    is (VXU_FENCE_CV)
    {
      io.vec_ackq.bits := Bits(1, 32)
      io.vec_ackq.valid := !io.hazard_to_issue.pending_memop && io.store_zero

      when (io.vec_ackq.valid)
      {
        state := VXU_FORWARD
      }
    }
    is (VXU_FENCE_V)
    {
      state := VXU_FORWARD
    }
  }

  val forward = (state === VXU_FORWARD) && (!decode_fence_cv.toBool && !decode_fence_v.toBool || !io.hazard_to_issue.pending_memop)

  io.vxu_cmdq.ready := 
    forward &&
    tvec_active && Bool(true) && mask_vxu_immq_valid && mask_vxu_imm2q_valid && mask_issue_ready &&
    io.irb_cmdb.ready && mask_irb_imm1b_ready && mask_irb_imm2b_ready && io.irb_cntb.ready

  io.vxu_immq.ready := 
    forward && 
    tvec_active && io.vxu_cmdq.valid && deq_vxu_immq && mask_vxu_imm2q_valid && mask_issue_ready &&
    io.irb_cmdb.ready && mask_irb_imm1b_ready && mask_irb_imm2b_ready && io.irb_cntb.ready

  io.vxu_imm2q.ready := 
    forward &&
    tvec_active && io.vxu_cmdq.valid && mask_vxu_immq_valid && deq_vxu_imm2q && mask_issue_ready &&
    io.irb_cmdb.ready && mask_irb_imm1b_ready && mask_irb_imm2b_ready && io.irb_cntb.ready

  io.irb_cmdb.valid := 
    forward && 
    !decode_fence_cv && !decode_fence_v && !fire_vcfg && !fire_setvl &&
    tvec_active && io.vxu_cmdq.valid && mask_vxu_immq_valid && mask_vxu_imm2q_valid && mask_issue_ready &&
    Bool(true) && mask_irb_imm1b_ready && mask_irb_imm2b_ready && io.irb_cntb.ready

  io.irb_imm1b.valid :=
    forward &&
    !decode_fence_cv && !decode_fence_v && !fire_vcfg && !fire_setvl &&
    tvec_active && io.vxu_cmdq.valid && deq_vxu_immq && mask_vxu_imm2q_valid && mask_issue_ready &&
    io.irb.cmdb.ready && deq_vxu_immq && mask_irb_imm2b_ready && io.irb_cntb.ready

  io.irb_imm2b.valid :=
    forward &&
    !decode_fence_cv && !decode_fence_v && !fire_vcfg && !fire_setvl &&
    tvec_active && io.vxu_cmdq.valid && mask_vxu_immq_valid && deq_vxu_imm2q && mask_issue_ready &&
    io.irb.cmdb.ready && mask_irb_imm1b_ready && deq_vxu_imm2q && io.irb_cntb.ready

  io.irb_cntb.valid :=
    forward &&
    !decode_fence_cv && !decode_fence_v && !fire_vcfg && !fire_setvl &&
    tvec_active && io.vxu_cmdq.valid && mask_vxu_immq_valid && mask_vxu_imm2q && mask_issue_ready &&
    io.irb.cmdb.ready && mask_irb_imm1b_ready && mask_irb_imm2b_ready && Bool(true)

  io.irb_cmdb.bits := io.vxu_cmdq.bits
  io.irb_imm1b.bits := io.vxu_immq.bits
  io.irb_imm2b.bits := io.vxu_imm2q.bits
  io.irb_cntb.bits := Bits(0, SZ_VLEN)

  io.valid.viu := 
    valid(0) && 
    tvec_active && io.vxu_cmdq.valid && mask_vxu_immq_valid && mask_vxu_imm2q_valid &&
    io.irb_cmdb.ready && mask_irb_imm1b_ready && mask_irb_imm2b_ready && io.irb_cntb.ready

  io.valid.vau0 := Bool(false)
  io.valid.vau1 := Bool(false)
  io.valid.vau2 := Bool(false)
  io.valid.amo := Bool(false)
  io.valid.utld := Bool(false)
  io.valid.utst := Bool(false)

  io.valid.vld := 
    valid(1) && 
    tvec_active && io.vxu_cmdq.valid && mask_vxu_immq_valid && mask_vxu_imm2q_valid &&
    io.irb_cmdb.ready && mask_irb_imm1b_ready && mask_irb_imm2b_ready && io.irb_cntb.ready

  io.valid.vst := 
    valid(2) && 
    tvec_active && io.vxu_cmdq.valid && mask_vxu_immq_valid && mask_vxu_imm2q_valid &&
    io.irb_cmdb.ready && mask_irb_imm1b_ready && mask_irb_imm2b_ready && io.irb_cntb.ready

  io.dhazard.vs := Bool(false)
  io.dhazard.vt := dhazard(0).toBool
  io.dhazard.vr := Bool(false)
  io.dhazard.vd := dhazard(1).toBool

  io.shazard.viu := Bool(false)
  io.shazard.vau0 := Bool(false)
  io.shazard.vau1 := Bool(false)
  io.shazard.vau2 := Bool(false)
  io.shazard.vgu := Bool(false)
  io.shazard.vlu := shazard(0).toBool
  io.shazard.vsu := shazard(1).toBool

  io.bhazard.r1w1 := bhazard(0).toBool
  io.bhazard.r2w1 := Bool(false)
  io.bhazard.r3w1 := Bool(false)
  io.bhazard.amo := Bool(false)
  io.bhazard.utld := Bool(false)
  io.bhazard.utst := Bool(false)
  io.bhazard.vld := bhazard(1).toBool
  io.bhazard.vst := bhazard(2).toBool

  io.fn.viu := Cat(M0,vmsrc,DW64,FP_,VIU_MOV)
  io.fn.vau0 := Bits(0,DEF_VAU0_FN)
  io.fn.vau1 := Bits(0,DEF_VAU1_FN)
  io.fn.vau2 := Bits(0,DEF_VAU2_FN)

  val vt_m1 = Cat(Bits(0,1),vt(4,0)) - UFix(1,1)
  val vd_m1 = Cat(Bits(0,1),vd(4,0)) - UFix(1,1)

  io.decoded.vs := Bits(0,DEF_REGLEN)
  io.decoded.vt := Mux(vt(5), vt_m1 + reg_nxregs, vt_m1) 
  io.decoded.vr := Bits(0,DEF_REGLEN)
  io.decoded.vd := Mux(vd(5), vd_m1 + reg_nxregs, vd_m1)
  io.decoded.vs_zero := Bool(true)
  io.decoded.vt_zero := vt === Bits(0,6)
  io.decoded.vr_zero := Bool(true)
  io.decoded.vd_zero := vd === Bits(0,6) && vd_valid
  io.decoded.mem.cmd := mem_cmd
  io.decoded.mem.typ := mem_type
  io.decoded.mem.typ_float := mem_type_float
  io.decoded.imm := imm
  io.decoded.imm2 := Mux(io.vxu_imm2q.ready, imm2, Cat(Bits(0,60), addr_stride))
  io.decoded.irb.imm1_rtag := io.irb_to_issue.imm1_rtag
  io.decoded.irb.cnt_rtag := io.irb_to_issue.cnt_rtag
  io.decoded.irb.update_imm1 := !io.valid.viu
}
